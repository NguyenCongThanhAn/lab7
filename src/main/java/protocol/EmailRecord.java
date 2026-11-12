package protocol;

public class EmailRecord {
   private final int id;
   private final String mailUid;
   private final String sender;
   private final String subject;
   private final String receivedDate;
   private final String bodyContent;
   private final String rawSource;
   private final String savedFilePath;

   public EmailRecord(int id, String mailUid, String sender, String subject, String receivedDate, String bodyContent, String rawSource, String savedFilePath) {
      this.id = id;
      this.mailUid = mailUid;
      this.sender = sender;
      this.subject = subject;
      this.receivedDate = receivedDate;
      this.bodyContent = bodyContent;
      this.rawSource = rawSource;
      this.savedFilePath = savedFilePath;
   }

   public int getId() {
      return this.id;
   }

   public String getMailUid() {
      return this.mailUid;
   }

   public String getSender() {
      return this.sender;
   }

   public String getSubject() {
      return this.subject;
   }

   public String getReceivedDate() {
      return this.receivedDate;
   }

   public String getBodyContent() {
      return this.bodyContent;
   }

   public String getRawSource() {
      return this.rawSource;
   }

   public String getSavedFilePath() {
      return this.savedFilePath;
   }

   public Object[] toRowData() {
      return new Object[]{this.id, this.sender, this.subject, this.receivedDate};
   }
}
