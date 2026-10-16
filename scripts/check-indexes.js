db.notifications.getIndexes().forEach(function(i) {
  print(i.name + ": " + JSON.stringify(i.key));
});