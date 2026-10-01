#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running Wear OS emulator (large round).
# Taps and crown turns can't be timed reliably through adb on a fresh emulator, so the sample opens
# each demo with fixed data from the `scene` extra.
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

install_sample
for scene in stopwatch rings list curved; do
  fresh_launch --es scene "$scene"
  capture "$scene"
done
