#!/bin/bash

YELLOW='\033[0;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
BOLD='\033[1m'
RESET='\033[0m'

command -v curl &> /dev/null || { echo -e "${RED}Error: 'curl' not found.${RESET}"; exit 1; }
command -v jq &> /dev/null || { echo -e "${RED}Error: 'jq' not found.${RESET}"; exit 1; }

display_step() {
  local step_number="$1"
  local description="$2"
  echo -e "${CYAN}${BOLD}# Step ${step_number}:${RESET} ${description}${RESET}"
}

separator() {
  echo -e "${YELLOW}--------------------------------------------------------------------------------${RESET}"
}

echo -e

display_step 1 "Create an RFP"
RESPONSE=$(curl -s http://localhost:8080/requirements)
TECHNICAL_SKILLS_ID=$(echo "$RESPONSE" | jq -r '.[] | select(.label == "Technical Skills") | .id')
EXPERIENCE_ID=$(echo "$RESPONSE" | jq -r '.[] | select(.label == "Experience") | .id')
DELIVERABLES_ID=$(echo "$RESPONSE" | jq -r '.[] | select(.label == "Deliverables") | .id')

RFP_CREATION_RESPONSE=$(curl -i -s -X POST http://localhost:8080/rfps \
--json "{
  \"title\": \"Senior Java Backend Engineer\",
  \"description\": \"Hiring a Senior Java Backend Engineer to design and implement scalable e-commerce web application.\",
  \"submissionDeadline\": \"2024-11-30T17:00:00\",
  \"requirements\": [
    {
      \"id\": \"$TECHNICAL_SKILLS_ID\",
      \"description\": \"Proficiency in Java, Spring Framework.\"
    },
    {
      \"id\": \"$EXPERIENCE_ID\",
      \"description\": \"At least 5 years of experience in backend development.\"
    },
    {
      \"id\": \"$DELIVERABLES_ID\",
      \"description\": \"Complete the onboarding process within 1 month.\"
    }
  ]
}")

RFP_URL=$(echo "$RFP_CREATION_RESPONSE" | grep -i "Location:" | awk '{print $2}' | tr -d '\r\n')
RFP_ID=$(echo "$RFP_URL" | awk -F'/' '{print $NF}')

if [ -z "$RFP_URL" ]; then
  echo -e "${RED}Failed to retrieve RFP URL from the Location header. Exiting.${RESET}"
  exit 1
else
  echo -e "${GREEN}RFP created with ID ${BOLD}$RFP_ID${BOLD}${RESET}"
fi
separator

display_step 2 "Publish the RFP"
curl -X POST "$RFP_URL/publish"
separator

display_step 3 "Creation & submission of 3 proposals from different vendors"
VENDORS_RESPONSE=$(curl -s http://localhost:8080/vendors)
VENDOR_IDS=($(echo "$VENDORS_RESPONSE" | jq -r '.[0:3] | .[].id'))

if [ ${#VENDOR_IDS[@]} -lt 3 ]; then
  echo -e "${RED}Error retrieving vendors. Exiting.${RESET}"
  exit 1
fi

for i in {1..3}; do
  VENDOR_ID="${VENDOR_IDS[$i-1]}"

  # Create the proposal
  PROPOSAL_CREATION_RESPONSE=$(curl -i -s -X POST http://localhost:8080/proposals \
  --json "{
    \"rfpId\": \"$RFP_ID\",
    \"vendorId\": \"$VENDOR_ID\",
    \"details\": \"Detailed description of the proposal $i\",
    \"amount\": $((9900 + i * 100)).00
  }")

  # Submit the proposal
  PROPOSAL_URL=$(echo "$PROPOSAL_CREATION_RESPONSE" | grep -i "Location:" | awk '{print $2}' | tr -d '\r\n')
  curl -s -X POST "$PROPOSAL_URL/submit"
done
separator

display_step 4 "Mark the 3 proposals as reviewed"
PROPOSALS_RESPONSE=$(curl -s "http://localhost:8080/proposals/search?rfpId=$RFP_ID")
PROPOSAL_IDS=($(echo "$PROPOSALS_RESPONSE" | jq -r '.[].id'))

if [ ${#PROPOSAL_IDS[@]} -lt 3 ]; then
  echo -e "${RED}Error retrieving proposals. Exiting.${RESET}"
  exit 1
fi

for PROPOSAL_ID in "${PROPOSAL_IDS[@]}"; do
  curl -s -X POST "http://localhost:8080/proposals/$PROPOSAL_ID/start-review"
done
separator

display_step 5 "Award 3rd Proposal"
curl -s -X POST "http://localhost:8080/proposals/$PROPOSAL_ID/award"
separator

echo -e "\n${CYAN}Related endpoints:${RESET}"
echo -e "GET $RFP_URL"
echo -e "GET http://localhost:8080/proposals/search?rfpId=$RFP_ID\n"
