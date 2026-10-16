@echo off
title Publish Cosmo Compendium OTA Update
powershell -ExecutionPolicy Bypass -File "%~dp0publish-update.ps1"
pause
