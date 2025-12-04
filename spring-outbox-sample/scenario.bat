@echo off
setlocal EnableDelayedExpansion

REM Check for curl
where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: 'curl' not found.
    exit /b 1
)

REM Check for jq
where jq >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: 'jq' not found. Please install jq from https://stedolan.github.io/jq/
    exit /b 1
)

echo.
echo ================================================================================
echo # Step 1: Create an RFP
echo ================================================================================

curl -s http://localhost:8080/requirements > temp_requirements.json

for /f "usebackq delims=" %%i in (`jq -r ".[] | select(.label == \"Technical Skills\") | .id" temp_requirements.json`) do set TECHNICAL_SKILLS_ID=%%i
for /f "usebackq delims=" %%i in (`jq -r ".[] | select(.label == \"Experience\") | .id" temp_requirements.json`) do set EXPERIENCE_ID=%%i
for /f "usebackq delims=" %%i in (`jq -r ".[] | select(.label == \"Deliverables\") | .id" temp_requirements.json`) do set DELIVERABLES_ID=%%i

echo {"title": "Senior Java Backend Engineer","description": "Hiring a Senior Java Backend Engineer to design and implement scalable e-commerce web application.","submissionDeadline": "2024-11-30T17:00:00","requirements": [{"id": "!TECHNICAL_SKILLS_ID!","description": "Proficiency in Java, Spring Framework."},{"id": "!EXPERIENCE_ID!","description": "At least 5 years of experience in backend development."},{"id": "!DELIVERABLES_ID!","description": "Complete the onboarding process within 1 month."}]} > temp_rfp.json

curl -i -s -X POST http://localhost:8080/rfps -H "Content-Type: application/json" -d @temp_rfp.json > temp_rfp_response.txt

REM Extract Location header more reliably
powershell -Command "$content = Get-Content temp_rfp_response.txt -Raw; if ($content -match 'Location:\s*(.+?)[\r\n]') { $matches[1].Trim() } else { '' }" > temp_location.txt
set /p RFP_URL=<temp_location.txt

REM Extract ID from URL using PowerShell
powershell -Command "'!RFP_URL!' -replace '.*/', ''" > temp_id.txt
set /p RFP_ID=<temp_id.txt

if "!RFP_ID!"=="" (
    echo Failed to retrieve RFP URL from the Location header. Exiting.
    echo Response:
    type temp_rfp_response.txt
    exit /b 1
) else (
    echo RFP created with ID !RFP_ID!
)

echo --------------------------------------------------------------------------------

echo.
echo ================================================================================
echo # Step 2: Publish the RFP
echo ================================================================================
curl -i -s -X POST "!RFP_URL!/publish" > temp_publish_response.txt
for /f "tokens=2" %%i in ('findstr /i "HTTP" temp_publish_response.txt') do (
    echo RFP published successfully - Status: %%i
    goto :publish_done
)
echo RFP published
:publish_done
echo --------------------------------------------------------------------------------

echo.
echo ================================================================================
echo # Step 3: Creation ^& submission of 3 proposals from different vendors
echo ================================================================================

curl -s http://localhost:8080/vendors > temp_vendors.json

set count=0
for /f "usebackq delims=" %%i in (`jq -r ".[0:3] | .[].id" temp_vendors.json`) do (
    set /a count+=1
    set VENDOR_ID_!count!=%%i
)

REM Proposal 1
echo {"rfpId": "!RFP_ID!","vendorId": "!VENDOR_ID_1!","details": "Detailed description of the proposal 1","amount": 10000.00} > temp_proposal.json
curl -i -s -X POST http://localhost:8080/proposals -H "Content-Type: application/json" -d @temp_proposal.json > temp_proposal_response.txt
powershell -Command "$content = Get-Content temp_proposal_response.txt -Raw; if ($content -match 'Location:\s*(.+?)[\r\n]') { $matches[1].Trim() } else { '' }" > temp_location.txt
set /p PROPOSAL_URL_1=<temp_location.txt
curl -s -X POST "!PROPOSAL_URL_1!/submit"
echo Proposal 1 submitted

REM Proposal 2
echo {"rfpId": "!RFP_ID!","vendorId": "!VENDOR_ID_2!","details": "Detailed description of the proposal 2","amount": 10100.00} > temp_proposal.json
curl -i -s -X POST http://localhost:8080/proposals -H "Content-Type: application/json" -d @temp_proposal.json > temp_proposal_response.txt
powershell -Command "$content = Get-Content temp_proposal_response.txt -Raw; if ($content -match 'Location:\s*(.+?)[\r\n]') { $matches[1].Trim() } else { '' }" > temp_location.txt
set /p PROPOSAL_URL_2=<temp_location.txt
curl -s -X POST "!PROPOSAL_URL_2!/submit"
echo Proposal 2 submitted

REM Proposal 3
echo {"rfpId": "!RFP_ID!","vendorId": "!VENDOR_ID_3!","details": "Detailed description of the proposal 3","amount": 10200.00} > temp_proposal.json
curl -i -s -X POST http://localhost:8080/proposals -H "Content-Type: application/json" -d @temp_proposal.json > temp_proposal_response.txt
powershell -Command "$content = Get-Content temp_proposal_response.txt -Raw; if ($content -match 'Location:\s*(.+?)[\r\n]') { $matches[1].Trim() } else { '' }" > temp_location.txt
set /p PROPOSAL_URL_3=<temp_location.txt
curl -s -X POST "!PROPOSAL_URL_3!/submit"
echo Proposal 3 submitted

echo.
echo --------------------------------------------------------------------------------

echo.
echo ================================================================================
echo # Step 4: Mark the 3 proposals as reviewed
echo ================================================================================

curl -s "http://localhost:8080/proposals/search?rfpId=!RFP_ID!" > temp_proposals.json

REM Debug: Show what we got (comment out if not needed)
REM type temp_proposals.json
REM echo.

for /f "usebackq delims=" %%i in (`jq -r ".[].id" temp_proposals.json`) do (
    curl -i -s -X POST "http://localhost:8080/proposals/%%i/start-review" > temp_review_response.txt
    for /f "tokens=2" %%j in ('findstr /i "HTTP" temp_review_response.txt') do echo Proposal %%i marked as reviewed - Status: %%j
    set LAST_PROPOSAL_ID=%%i
)

echo.
echo --------------------------------------------------------------------------------

echo.
echo ================================================================================
echo # Step 5: Award 3rd Proposal
echo ================================================================================
curl -i -s -X POST "http://localhost:8080/proposals/!LAST_PROPOSAL_ID!/award" > temp_award_response.txt
for /f "tokens=2" %%j in ('findstr /i "HTTP" temp_award_response.txt') do echo Proposal !LAST_PROPOSAL_ID! awarded - Status: %%j
echo --------------------------------------------------------------------------------

echo.
echo Related endpoints:
echo GET !RFP_URL!
echo GET http://localhost:8080/proposals/search?rfpId=!RFP_ID!
echo.

REM Cleanup
del temp_*.json temp_*.txt 2>nul

endlocal
