# BingoCV 数据库脚本

在 MySQL 8.x 中执行：

```sql
SOURCE E:/projects/java/personal/bingoCV/db/bingocv_schema.sql;
```

或者用命令行：

```bash
mysql -uroot -proot < E:/projects/java/personal/bingoCV/db/bingocv_schema.sql
```

脚本包含现有简历核心表，以及模板、积分、签到、任务、分享、短链、配置、支付订单等后续 plan 需要的表。
