## Setup SQL:
```sql
CREATE TABLE partitions_sample(
	id SERIAL NOT NULL,
	partition_name TEXT NOT NULL,
	value TEXT
) PARTITION BY LIST (partition_name);

CREATE TABLE IF NOT EXISTS partitions_sample_apple PARTITION OF partitions_sample
    FOR VALUES IN ('apple')
```
