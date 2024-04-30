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
Load new data, but still use old data:
<img src="upload-to-new-partition.png" />
Then switch to the new partition:
<img src="after-upload.png" />

### Pro: Create Index with Little Downtime

#### Without Partitions, Creating New Index on Large Table Is Tricky
* Just run `CREATE INDEX` - blocks table for write, can be a long time
* Try creating index with `CONCURRENT` - it can fail, again and again. And it will use lots of resources every time.
* Create new table with new index. Eventually migrate to it. Lots of work.

  

### Con: Some queries are slower

Suppose we have an index that does not include column(s) we partition on:
```
CREATE INDEX things__id ON things(thing_id)
```
Also suppose that `thing_id` is highly selective, which means that usually only one row matches any given `thing_id`.
<br\>
Because Postgres does not know which partition the rows we are looking for are stored in. So it will query all partitions
