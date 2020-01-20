package pl.rozbijbank.db.model;

import java.util.Date;

public interface Task extends BasicModel{

     String getTitle();

     long getPromoId();

     long getBankId();
   
     Date getStartDate();

     Date getEndDate() ;

     String getDescription();

     String getNote();

     short getTaskType();

     short getMonthAfterSigning();
   
     double getAmount();
    
     boolean isCompleted();
    
     double getActualAmount();

     long getParticipationId() ;

     Date getUserStartDate() ;

     Date getUserEndDate() ;

     String getUri();

     int getDaysAfterSigning() ;
}
