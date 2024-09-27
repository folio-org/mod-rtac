
## 3.6.1 2024-09-27

* Do not include closed and cancelled hold in the hold count [MODRTAC-112](https://folio-org.atlassian.net/browse/MODRTAC-112)

## 3.6.0 2024-03-20

* Upgrade mod-rtac to RMB 35.2.0, Vert.x 4.5.4 [MODRTAC-109](https://folio-org.atlassian.net/browse/MODRTAC-109)
* Add holdingsCopyNumber and itemCopyNumber to rtac response ([EDGRTAC-81](https://folio-org.atlassian.net/browse/EDGRTAC-81))
* Add new displaySummarry field ([MODRTAC-107](https://folio-org.atlassian.net/browse/MODRTAC-107))
* Update from Java 11 to Java 17 ([MODRTAC-104](https://folio-org.atlassian.net/browse/MODRTAC-104))
* Remove commons-io 2.6 dependency fixing CVE-2021-29425 ([MODRTAC-106](https://folio-org.atlassian.net/browse/MODRTAC-106))
* Add effective location code and material type single and batch response ([MODRTAC-105](https://folio-org.atlassian.net/browse/MODRTAC-105))
* Enable API-related GitHub Workflows, replace those Jenkins stages ([MODRTAC-103](https://folio-org.atlassian.net/browse/MODRTAC-103))

## 3.5.0 2023-02-14

* Added personal data disclosure form (MODRTAC-96)

## 3.4.0 2022-10-18

* Upgraded RMB to 35.0.0 ([MODRTAC-99](https://issues.folio.org/browse/MODRTAC-99))
* Separate error message for not found instances ([MODRTAC-88](https://issues.folio.org/browse/MODRTAC-88))

## 3.3.0 2022-06-14

* requests containing records with and without items now return holdings data for all records ([MODRTAC-89](https://issues.folio.org/browse/MODRTAC-89))

## 3.2.0 2022-02-22

* Upgrade to Log4J 2.16.0. (CVE-2021-44228) ([MODRTAC-81](https://issues.folio.org/browse/MODRTAC-81))
* Upgrade to RMB 33.2.4 ([MODRTAC-86](https://issues.folio.org/browse/MODRTAC-86))

## 3.1.0 2021-10-05

* rtac-batch endpoint now correctly returns data for instances with holdings but no items ([MODRTAC-56](https://issues.folio.org/browse/MODRTAC-56))

## 3.0.0 2021-06-17

* `embed_postgres` command line option is no longer supported ([MODRTAC-58](https://issues.folio.org/browse/MODRTAC-58))
* Upgrades to RAML-Module-Builder 33.0.0 ([MODRTAC-58](https://issues.folio.org/browse/MODRTAC-58))
* Upgrades to Vert.x 4.1.0.CR1 ([MODRTAC-58](https://issues.folio.org/browse/MODRTAC-58))

## 2.1.0 2021-03-16

* Upgrades to RAML Module Builder 32.1.0 ([MODRTAC-54](https://issues.folio.org/browse/MODRTAC-54))
* Upgrades to vert.x 4.0.0 ([MODRTAC-54](https://issues.folio.org/browse/MODRTAC-54))


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
 * Requires either `circulation` 3.0, 4.0, 5.0, 6.0 or 7.0 ([MODRTAC-11](https://issues.folio.org/browse/MODRTAC-11), [MODRTAC-12](https://issues.folio.org/browse/MODRTAC-12))

## 1.2.1 2018-12-04
 * Requires `inventory` version 8.0 ([MODRTAC-9](https://issues.folio.org/browse/MODRTAC-9))
 * Requires `holdings-storage` versions `1.2 2.0 3.0` ([MODRTAC-10](https://issues.folio.org/browse/MODRTAC-10))

## 1.2.0 2018-11-12
 * Added missing description fields to JSON schemas ([MODRTAC-5](https://issues.folio.org/browse/MODRTAC-5))
 * Requires either `circulation` 3.0, 4.0 or 5.0 ([MODRTAC-6](https://issues.folio.org/browse/MODRTAC-6))
 * Requires either `inventory` 5.3, 6.0 or 7.0 ([MODRTAC-4](https://issues.folio.org/browse/MODRTAC-4))
 * Requires either `holdings-storage` 1.2 or 2.0 ([MODRTAC-4](https://issues.folio.org/browse/MODRTAC-4))

## 1.1.0 2018-09-07
 * Updated the query limit value from `100` to `Integer.MAX_VALUE` ([MODRTAC-2](https://issues.folio.org/browse/MODRTAC-2))
 * Requires either `circulation` 3.0 or 4.0 ([MODRTAC-3](https://issues.folio.org/browse/MODRTAC-3), [CIRC-136](https://issues.folio.org/browse/CIRC-136))

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
