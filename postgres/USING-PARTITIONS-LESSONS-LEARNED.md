## Using Partitioned Tables in Highly Avaiable Systems: Lessons Learned
### TL/DR: Partitioned Tables are highly useful in some cases, not the best fit in other ones.
#### Pros
* No VACUUM overhead after dropping a partition
* Large data loads done slowly, without much impact on server's performance
* Can add an index on large partitioned table without much downtime
#### Cons
* Some queries are slower
* More difficult to ensure uniqueness
* More difficult to implement `UPSERT` aka `INSERT ... ON CONFLICT(...) DO UPDATE`
