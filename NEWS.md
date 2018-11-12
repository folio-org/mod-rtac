## 1.2.0 2018-11-12
 * Added missing description fields to JSON schemas (MODRTAC-5)
 * Requires either `circulation` 3.0, 4.0 or 5.0 (MODRTAC-6)
 * Requires either `inventory` 5.3, 6.0 or 7.0 (MODRTAC-4)
 * Requires either `holdings-storage` 1.2 or 2.0 (MODRTAC-4)

## 1.1.0 2018-09-07
 * Updated the query limit value from `100` to `Integer.MAX_VALUE` (MODRTAC-2)
 * Requires either `circulation` 3.0 or 4.0 (MODRTAC-3, CIRC-136)

## 1.0.1 2018-06-29
 * Added support for the new way item locations are set in mod-inventory.
 * The RTAC "dueDate" field was incorrectly being set to the current date/time
   in the case where "dueDate" was not set in the FOLIO loan. In this case,
   RTAC should not set the "dueDate" in the response.
 * Updated RMB dependency to 19.1.3.

## 1.0.0 2018-05-18
 * POM file cleanup

## 0.0.2 2018-04-25
 * Update circulation dependency to 3.0

## 0.0.1 2018-04-20
 * Initial work
