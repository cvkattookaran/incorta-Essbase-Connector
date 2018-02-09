
# incorta-Essbase-Connector

usage: EsbDataQuery

| Parameter     | Use                               |
|:--------------|:----------------------------------|
| -App <-App=applicationname> | Essbase Application <required> |
| -checkSyntax | Check MDX syntax |
| -D <-D> |  Use this if you are using encryption |
| -Db <-Db=databasename>  | Essbase Database <required> |
| -delimiter <-delimiter=delimitercharacter>  | Delimiter character |
| -encrypt <-encrypt=<encryptstring>>         | Use this for encrypting strings|
| -getData                                    | Get data from the Essbase database \<required\ |
| -help                                       | prints usage|
| -key <-key=key>                             | Key to encrypt strings|
| -MDX <-MDX=MDXQuery>                        | MDX query to get data \<required\|
| -p <-p=password>                            | Password for connecting to Essbase \<required\>|
| -S <-S=servername>                          | Essbase Server Name|
| -u <-u=username>                            | Username for connecting to Essbase \<required\|

```java
-u=admin -p=password -S=OEL5-INTEKGRATE -App=Sample -Db=Basic -getData -MDX="SELECT {[Jan],[Feb],[Apr]} ON COLUMNS,{[Sales],[COGS]} ON ROWS from [Sample.Basic] WHERE [New York];" -delimiter="|"

```
You can use check MDX syntax using

```java
-u=admin -p=password -S=OEL5-INTEKGRATE -App=Sample -Db=Basic -getData -MDX="SELECT {[Jan],[Feb]} ON AXIS(1),{[Sales],[COGS]} ON AXIS(0),{[Actual]} ON AXIS(2), {[100-10]} on AXIS(3) from [Sample.Basic]" -checkSyntax
```
You can also use with expression MDX

```plain
WITH MEMBER [Measures].[Max Qtr2 Sales] AS 'Max ({[Year].[Qtr2]},[Measures].[Sales])' SELECT {[Measures].[Max Qtr2 Sales]} ON COLUMNS, {[Product].children} ON ROWS FROM Sample.Basic;
```