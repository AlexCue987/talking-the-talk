## Using Partitioned Tables in Highly Avaiable Systems: Lessons Learned
### TL/DR: Partitioned Tables are highly useful in some cases, not the best fit in other ones.
#### Pros
* No VACUUM overhead after dropping a partition
* Large data loads done without much impact on server's performance
* Can add an index on large partitioned table without much downtime
#### Cons
* Some queries are slower
* More difficult to ensure uniqueness
* More difficult to implement `UPSERT` aka `INSERT ... ON CONFLICT(...) DO UPDATE`
### Pro: No VACUUM Overhead After Dropping A Partition
This is the main reason we are using partitions. Dropping a partition does not add any work for `VACUUM`. We've had databases that were written to and read from 24/7, but at leat 90% of the resources were spent on deleting old data. So switching to partitions rendered those databases from very busy to mostly idle.
### Pro: Large Data Loads Done Without Much Impact on Server's Performance
