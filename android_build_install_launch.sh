#!/usr/bin/env bash
set -euo pipefail

ADB_SERIAL=""
APP_MODULE=""
APP_ID=""
APK_PATH=""
VARIANT="debug"
RUN_CLEAN="false"

usage() {
  cat <<'EOF'
Usage: ./android_build_install_launch.sh [options]

Build Android APK with --no-daemon, install it, and launch app.

Options:
  -s, --serial <device_serial>   Target adb device serial
  -v, --variant <variant>        Build variant (default: debug), e.g. debug/release/freeDebug
  -m, --module <module_name>     App module name (auto-detect by default)
      --clean                    Run clean before assemble
  -h, --help                     Show this help
EOF
}

to_pascal_case() {
  local input="$1"
  local first="${input:0:1}"
  local rest="${input:1}"
  printf '%s%s' "$(tr '[:lower:]' '[:upper:]' <<<"$first")" "$rest"
}

find_app_module() {
  local gradle_file module
  for gradle_file in ./*/build.gradle ./*/build.gradle.kts; do
    [ -f "$gradle_file" ] || continue
    if grep -Eq "com.android.application|apply[[:space:]]+plugin:[[:space:]]*['\"]com.android.application['\"]" "$gradle_file"; then
      module="${gradle_file#./}"
      echo "${module%%/*}"
      return 0
    fi
  done
  return 1
}

extract_application_id() {
  local gradle_file="$1"
  local line
  line="$(
    grep -Eo "applicationId[[:space:]]*=?[[:space:]]*['\"][^'\"]+['\"]" "$gradle_file" \
      | head -n 1 || true
  )"
  if [ -z "$line" ]; then
    return 1
  fi
  printf '%s\n' "$line" | sed -E "s/.*['\"]([^'\"]+)['\"].*/\\1/"
}

find_variant_apk() {
  local module="$1"
  local variant="$2"
  local apk_root="./${module}/build/outputs/apk"
  local variant_lower
  variant_lower="$(tr '[:upper:]' '[:lower:]' <<<"$variant")"
  [ -d "$apk_root" ] || return 1
  find "$apk_root" -type f \( -name "*${variant}.apk" -o -name "*${variant_lower}.apk" -o -name "*.apk" \) \
    | awk -v v="$variant_lower" '
        {
          p=tolower($0)
          if (index(p, "/" v "/") > 0 || index(p, "-" v ".apk") > 0 || index(p, v ".apk") > 0) {
            print
            exit
          }
          all[NR]=$0
        }
        END {
          if (NR > 0 && length(all[1]) > 0) print all[1]
        }
      ' | head -n 1
}

parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      -s|--serial)
        [ "${2:-}" != "" ] || { echo "Error: $1 requires a value." >&2; exit 1; }
        ADB_SERIAL="$2"
        shift 2
        ;;
      -v|--variant)
        [ "${2:-}" != "" ] || { echo "Error: $1 requires a value." >&2; exit 1; }
        VARIANT="$2"
        shift 2
        ;;
      -m|--module)
        [ "${2:-}" != "" ] || { echo "Error: $1 requires a value." >&2; exit 1; }
        APP_MODULE="$2"
        shift 2
        ;;
      --clean)
        RUN_CLEAN="true"
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Error: Unknown option '$1'" >&2
        usage >&2
        exit 1
        ;;
    esac
  done
}

resolve_device() {
  if [ -n "$ADB_SERIAL" ]; then
    local state
    state="$(adb -s "$ADB_SERIAL" get-state 2>/dev/null || true)"
    if [ "$state" != "device" ]; then
      echo "Error: adb device '$ADB_SERIAL' is not online." >&2
      exit 1
    fi
    return
  fi

  mapfile -t DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [ "${#DEVICES[@]}" -eq 0 ]; then
    echo "Error: No online adb device found." >&2
    exit 1
  fi
  if [ "${#DEVICES[@]}" -gt 1 ]; then
    echo "Error: Multiple adb devices detected. Please specify target with -s." >&2
    printf 'Detected devices:\n' >&2
    printf '  %s\n' "${DEVICES[@]}" >&2
    exit 1
  fi
  ADB_SERIAL="${DEVICES[0]}"
}

main() {
  parse_args "$@"

  if ! command -v adb >/dev/null 2>&1; then
    echo "Error: adb not found in PATH." >&2
    exit 1
  fi

  local gradle_cmd
  if [ -f "./gradlew" ]; then
    gradle_cmd="./gradlew"
  elif [ -f "./gradlew.bat" ]; then
    gradle_cmd="./gradlew.bat"
  else
    echo "Error: gradle wrapper not found in current directory." >&2
    exit 1
  fi

  if [ -z "$APP_MODULE" ]; then
    if ! APP_MODULE="$(find_app_module)"; then
      echo "Error: Cannot find Android application module (com.android.application)." >&2
      exit 1
    fi
  fi

  if [ -f "./${APP_MODULE}/build.gradle" ]; then
    APP_ID="$(extract_application_id "./${APP_MODULE}/build.gradle")"
  elif [ -f "./${APP_MODULE}/build.gradle.kts" ]; then
    APP_ID="$(extract_application_id "./${APP_MODULE}/build.gradle.kts")"
  else
    echo "Error: Cannot find build script for module '${APP_MODULE}'." >&2
    exit 1
  fi

  if [ -z "$APP_ID" ]; then
    echo "Error: Cannot resolve applicationId from module '${APP_MODULE}'." >&2
    exit 1
  fi

  resolve_device

  local assemble_task=":${APP_MODULE}:assemble$(to_pascal_case "$VARIANT")"
  local clean_task=":${APP_MODULE}:clean"

  echo "[1/3] Building APK (module=${APP_MODULE}, variant=${VARIANT})..."
  if [ "$RUN_CLEAN" = "true" ]; then
    "$gradle_cmd" --no-daemon "$clean_task" "$assemble_task"
  else
    "$gradle_cmd" --no-daemon "$assemble_task"
  fi

  if ! APK_PATH="$(find_variant_apk "$APP_MODULE" "$VARIANT")"; then
    echo "Error: Cannot find APK under ${APP_MODULE}/build/outputs/apk for variant '${VARIANT}'." >&2
    exit 1
  fi

  echo "[2/3] Installing APK on device: $ADB_SERIAL"
  adb -s "$ADB_SERIAL" install -r "$APK_PATH"

  echo "[3/3] Launching app: $APP_ID"
  adb -s "$ADB_SERIAL" shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1

  echo "Done: $APK_PATH"
}

main "$@"
