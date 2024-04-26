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
Sometimes we need to upload large amounts of new data, mostly replacing existing rows. For data consistency, the switching for old version to new must happen at once. Without partitions, such loads must be implemented as one big transaction, which is uses up a lot of servers' resources and impacts server's performance. Because a lot of rows are updated, such uploads are followed by a lot of `VACCUM`ing, which causes the second impact to the performance.
<br/>
<br/>
Using partitions, we can:
* Create a new partition
* Load the same amount of data into the new partition. We can do it slowly, so that we do not impact server's performance.
* Optional benefit: we can compare old data in old partition against new data in new partition
* Switch to using new partition
* Drop old partition - no work for `VACUUM`
<br/>
![Loading Data into New Partition While API Still Uses Old One](https://github.com/AlexCue987/talking-the-talk/blob/main/postgres/upload-to-new-partition.png)
