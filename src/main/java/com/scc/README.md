



## User Guide & Operational Workflows

### 1. Initial Launch & Default Administrator Access

Upon the first run, the system automatically builds the SQLite database file (`JeepneyOperations.db`) and seeds a default Administrator account.

* **Login Option:** Select `1` from the main menu.
* **Default Username:** `admin`
* **Default Password:** `admin123`

---

### 2. Staff Account Registration

All newly registered Driver or Conductor accounts start in a **`PENDING`** state and cannot log in until an Administrator approves them.

1. Select **`2. Register Account`** from the main menu.
2. Enter the desired **Username** and **Password**.
3. Select Role:
* Option `1` for **Driver**
* Option `3` for **Conductor**


4. The system confirms registration with: `Status: PENDING (Awaiting Admin Approval)`.

---

### 3. Administrator Approval Workflow

1. Log in using the **Admin** credentials (`admin` / `admin123`).
2. Select **`1. View Pending Registrations`** to inspect accounts waiting for access.
3. Select **`2. Approve/Reject User`**:
* Enter the **User ID** of the pending account.
* Enter **`1`** to **Approve** (or **`2`** to Reject).


4. Return to the main menu. Approved users can now log in to their assigned role panels.

---

### 4. Conductor Panel Operations

Log in using an approved **Conductor** account to handle fare transactions and seating:

* **Add Passenger & Collect Fare:**
1. Input drop-off destination name (e.g., *Mambaling*).
2. Input travel distance in kilometers (e.g., *6.5*).
3. Select passenger category:
* `1` — Regular (Full Fare)
* `2` — Student (20% Discount)
* `3` — Senior Citizen (20% Discount)
* `4` — PWD (20% Discount)


4. The system computes the distance-based fare, applies discounts if applicable, logs the record to SQLite, and updates vehicle seating capacity (+1 occupied seat).


* **View Passenger Records:**
* Displays a table history of all ticketed passengers, distances, passenger types, and calculated fares.


* **Reset Trip Seats (New Pasada):**
* Resets the active seat counter back to **0/18 Occupied** when passengers disembark at the end of a route loop.



---

### 5. Driver Panel Operations

Log in using an approved **Driver** account to access operational metrics and vehicle checks:

* **View Daily Financial Summary:**
* Calculates and displays Gross Earnings, Total Expenses, and computed **Net Take-Home Pay**.


* **Log Gas/Fuel Expense:**
* Input daily fuel purchases in Pesos (e.g., *1200.00*). The entry is saved to the database and automatically deducted from gross earnings.


* **Vehicle Maintenance Checklist:**
* Displays active vehicle safety checks (*Engine Oil*, *Brakes*, *Tires*).
* Select an item ID to update its operational status (e.g., *Good*, *Needs Check*, *Replaced*).


* **Update Fare Matrix:**
* Dynamically adjusts base fare rates or extra kilometer charges across the application without requiring code modifications.



```

```