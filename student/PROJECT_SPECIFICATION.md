# QuickCart Behavioural Specification

Students should treat this specification as authoritative. If implementation and specification disagree, the mismatch is a defect candidate.

## Pricing
1. Each line subtotal is `unit price × quantity`.
2. A **5% bulk discount** applies to a line when its quantity is **10 or more**.
3. A **GOLD** customer receives an additional **10% discount** on merchandise after any bulk discounts.
4. An active coupon is applied after bulk and GOLD discounts if discounted merchandise is at least **$50.00**. Coupon validity includes **both start and end dates**.
5. Discounted merchandise cannot become negative.
6. Shipping is **$8.99**, but is free when discounted merchandise is **$100.00 or more**.
7. Tax is applied to **discounted merchandise plus shipping**. Rates: ON 13%, BC 12%, AB 5%.
8. Tax is rounded to two decimal places using HALF_UP. The final total is merchandise + shipping + tax.

## Ordering
1. Every item must have sufficient inventory. An order requesting exactly the available quantity is valid.
2. If any item is out of stock, payment must not be attempted and inventory must not change.
3. If inventory is sufficient, payment authorization is attempted for the quoted total.
4. Inventory is reserved **only after payment authorization succeeds**.
5. If payment is declined, inventory must remain unchanged.

## Input constraints
- Cart must contain at least one item.
- Item quantity must be positive.
- Product price must be nonnegative.
- Supported tax provinces are ON, BC, and AB.
