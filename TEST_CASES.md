# Functional Test Suite: Favourite Payee Application

This document outlines 8 key test cases to verify the application's core functionality, security, and intelligent features.

---

### TC-01: Successful Authentication
- **Objective**: Verify that a user can log in with a valid Customer ID.
- **Steps**:
  1. Navigate to the login page.
  2. Enter a valid ID (e.g., `1`).
  3. Click "Login".
- **Expected Result**: User is issued a JWT and redirected to the `/payees` dashboard.

### TC-02: Search & Filter Functionality
- **Objective**: Verify that the search bar correctly filters the payee list.
- **Steps**:
  1. Log in and view the "All Accounts" list.
  2. Type a specific name (e.g., "Profesor") into the search bar.
- **Expected Result**: The list updates in real-time to show only matching accounts.

### TC-03: Automatic Bank Resolution (IBAN)
- **Objective**: Verify that the system resolves the bank name based on the IBAN.
- **Steps**:
  1. Click "Add new account".
  2. Enter an IBAN starting with `ABCD1234...`.
- **Expected Result**: The "Bank" field should automatically populate with "Nairobi Bank".

### TC-04: Smart Favourite Promotion (Frequency)
- **Objective**: Verify that frequent interactions promote a payee to the top.
- **Steps**:
  1. Identify a payee in the "All Accounts" list.
  2. Click the card 5-10 times consecutively.
  3. Refresh the page or wait for the update.
- **Expected Result**: That payee should now appear in the "Smart Favourites" section.

### TC-05: Time-Based Smart Ranking
- **Objective**: Verify that interactions at specific times influence the ranking.
- **Steps**:
  1. Log an interaction for Payee A at the current hour.
  2. Wait or simulate a time shift.
- **Expected Result**: Payee A should have a higher score when viewed during a similar time block in the future.

### TC-06: Read-Only Data View
- **Objective**: Verify that clicking a card shows details without entering edit mode.
- **Steps**:
  1. Click the body of any payee card.
- **Expected Result**: Navigation to `/payees/view/:id` where all fields are disabled and read-only.

### TC-07: Payee Account Modification
- **Objective**: Verify that the dedicated "Edit" button allows for data updates.
- **Steps**:
  1. Click the small "Edit" button on a payee card.
  2. Change the name or IBAN.
  3. Click "Save".
- **Expected Result**: The record is updated in the database and reflected in the list.

### TC-08: Security & Auth Guard
- **Objective**: Verify that the application protects private routes.
- **Steps**:
  1. Log out.
  2. Attempt to manually navigate to `http://localhost:4200/payees`.
- **Expected Result**: The user is blocked and automatically redirected to the login page.
