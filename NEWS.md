## 2.1.0 IN-PROGRESS

* [MODRTAC-54] (https://issues.folio.org/browse/MODRTAC-54): Upgrade to RAML Module Builder 32.x 

## 2.0.1 2020-11-04
 * [MODRTAC-48](https://issues.folio.org/browse/MODRTAC-48): Issues in batch API when querying an instance with more than 50 holdings

## 2.0.0 2020-10-14
 * [MODRTAC-34](https://issues.folio.org/browse/MODRTAC-34): REST batching support
 * [MODRTAC-38](https://issues.folio.org/browse/MODRTAC-38): For 1 User Rtac takes ~15 seconds to get holdings record with open loans
 * [MODRTAC-41](https://issues.folio.org/browse/MODRTAC-41): Update to java 11
 * [MODRTAC-37](https://issues.folio.org/browse/MODRTAC-47): Better handle periodicals in response
 
## 1.5.0 2020-06-12
 * [MODRTAC-33](https://issues.folio.org/browse/MODRTAC-33): Upgrade RMB to 30.0.2

## 1.4.0 2020-03-18
 * [MODRTAC-17](https://issues.folio.org/browse/MODRTAC-17): Removing Volume leaves "tl ()" displaying in EDS
 * [MODRTAC-28](https://issues.folio.org/browse/MODRTAC-28): Migrate to new major version of item-storage, inventory, circulation

## 1.3.0 2019-12-04
 * [MODRTAC-25](https://issues.folio.org/browse/MODRTAC-25): Update RMB to 29.1.0
 * [MODRTAC-20](https://issues.folio.org/browse/MODRTAC-20): Add `holdings-storage` 4.0
 * [MODRTAC-19](https://issues.folio.org/browse/MODRTAC-19): Add `circulation` 9.0

## 1.2.4 2019-09-12
 * [MODRTAC-16](https://issues.folio.org/browse/MODRTAC-16): adding suffix and prefix to call numbers.

## 1.2.3 2019-07-24
 * [MODRTAC-15](https://issues.folio.org/browse/MODRTAC-15): Requires either `login` `5.0` or
   `6.0`
 * [MODRTAC-14](https://issues.folio.org/browse/MODRTAC-14): Return "volume" in the RTAC response

## 1.2.2 2019-03-22
 * Requires either `circulation` 3.0, 4.0, 5.0, 6.0 or 7.0 (MODRTAC-11, MODRTAC-12)

## 1.2.1 2018-12-04
 * Requires `inventory` version 8.0 (MODRTAC-9)
 * Requires `holdings-storage` versions `1.2 2.0 3.0` (MODRTAC-10)

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
