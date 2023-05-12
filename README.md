This project showcases using JSON to represent data

As of 2023-05-12 It loads some JSON data, and creates an index.

You can then use redis-cli or another tool or program such as: https://github.com/owentechnologist/multiThreadSearchTest to perform Sample searches:


``` 
> FT.SEARCH idx_mcc "@mccid:8062" return 3 '$.mccver1[?(@.id =~ "(?i)8062")].description' AS MATCHING_DESCRIPTION LIMIT 0 2
1) "1"
2) "mccjson"
3) 1) "MATCHING_DESCRIPTION"
   2) "Hospitals"
```

Another one using description as the matching field:
``` 
> FT.SEARCH idx_mcc "@description:Hosp*" return 6 '$.mccver1[?(@.description =~ "(?i)Hosp")].description' AS MATCHING_DESCRIPTION '$.mccver1[?(@.description =~ "(?i)Hosp")].id' AS MATCHING_ID LIMIT 0 3 DIALECT 3
1) "1"
2) "mccjson"
3) 1) "MATCHING_DESCRIPTION"
   2) "[\"Hospitality International\",\"Medical, Dental Ophthalmic, Hospital Equipment And Supplies\",\"Hospitals\"]"
   3) "MATCHING_ID"
   4) "[\"3595\",\"5047\",\"8062\"]"
```
