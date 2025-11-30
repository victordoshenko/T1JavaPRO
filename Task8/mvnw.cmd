@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
if not defined MAVEN_PROJECTBASEDIR set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROPERTIES%" (
  echo ERROR: Could not find "%WRAPPER_PROPERTIES%"
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  for /f "tokens=1,2 delims==" %%A in ('type "%WRAPPER_PROPERTIES%" ^| find /i "wrapperUrl"') do (
    set "WRAPPER_URL=%%B"
  )
  if not defined WRAPPER_URL (
    echo ERROR: wrapperUrl not set in %WRAPPER_PROPERTIES%
    exit /b 1
  )
  if not exist "%~dp0.mvn" mkdir "%~dp0.mvn" >nul 2>&1
  if not exist "%~dp0.mvn\wrapper" mkdir "%~dp0.mvn\wrapper" >nul 2>&1
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'" || (
    echo ERROR: Failed to download Maven Wrapper jar from %WRAPPER_URL%
    exit /b 1
  )
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_EXE%" goto runWrapper

set "JAVA_EXE=java"
where java >nul 2>nul || (
  echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
  exit /b 1
)

:runWrapper
"%JAVA_EXE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%


