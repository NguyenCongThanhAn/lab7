package callback;

import protocol.EmailRecord;

public interface MailboxCallback {
   void onOpenMail(EmailRecord var1);

   void onDeleteMail(EmailRecord var1);
}
