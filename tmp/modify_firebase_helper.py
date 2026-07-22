import sys

filepath = "app/src/main/java/com/example/data/repository/LyoFirebaseHelper.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Replace the doc occurrences
target_doc = 'rejectionReason = doc.getString("rejectionReason") ?: ""'
replacement_doc = 'rejectionReason = doc.getString("rejectionReason") ?: "",\n                                                 gstAmount = doc.getDouble("gstAmount") ?: 0.0'

# Count occurrences before replacing
doc_count = content.count(target_doc)
print(f"Found {doc_count} occurrences of target_doc")

content = content.replace(target_doc, replacement_doc)

# Replace the snapshot occurrences
target_snapshot = 'rejectionReason = snapshot.getString("rejectionReason") ?: ""'
replacement_snapshot = 'rejectionReason = snapshot.getString("rejectionReason") ?: "",\n                             gstAmount = snapshot.getDouble("gstAmount") ?: 0.0'

snap_count = content.count(target_snapshot)
print(f"Found {snap_count} occurrences of target_snapshot")

content = content.replace(target_snapshot, replacement_snapshot)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

print("Modification complete!")
