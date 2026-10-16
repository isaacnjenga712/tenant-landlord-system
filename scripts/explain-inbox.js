const stats = db.notifications.find({
  organizationId: UUID("00000000-0000-0000-0000-000000000001"),
  recipientPublicId: UUID("00000000-0000-0000-0001-000000000001")
}).sort({createdAt: -1}).limit(20).explain("executionStats").executionStats;

print("nReturned:          " + stats.nReturned);
print("executionTimeMs:    " + stats.executionTimeMillis);
print("totalDocsExamined:  " + stats.totalDocsExamined);
print("totalKeysExamined:  " + stats.totalKeysExamined);
print("stage:              " + stats.executionStages.stage);

if (stats.executionStages.inputStage) {
  print("inputStage:         " + stats.executionStages.inputStage.stage);
  if (stats.executionStages.inputStage.indexName) {
    print("indexName:          " + stats.executionStages.inputStage.indexName);
  }
  if (stats.executionStages.inputStage.inputStage) {
    print("innerStage:         " + stats.executionStages.inputStage.inputStage.stage);
    if (stats.executionStages.inputStage.inputStage.indexName) {
      print("innerIndexName:     " + stats.executionStages.inputStage.inputStage.indexName);
    }
  }
}