# mod-rtac

Copyright (C) 2018-2024 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

Microservice to allow 3rd party discovery services to determine the availability of FOLIO inventory.

## Additional information

The expectation is that the **mode of issuance "serial"** and **nature of content "journal" and "newspaper"** are present in the target system. The module uses these values to qualify an instance as a periodical.

### Issue tracker

See project [MODRTAC](https://issues.folio.org/browse/MODRTAC)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

### Settings
LOAN_TENANT setting used to specify the tenant from where Loan data is fetched.
Example payload: 
```
{
"id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
"scope": "mod-rtac",
"key": "LOAN_TENANT",
"value": "test_central_tenant"
}
```

