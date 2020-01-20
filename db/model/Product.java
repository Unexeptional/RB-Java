package pl.rozbijbank.db.model;

import java.util.Date;

public  interface Product extends BasicModel{

    String getTitle();

    long getBankId() ;

    int getProductType() ;

    String getDescription();

    boolean isInactive() ;

    Date getStartDate() ;

    Date getEndDate() ;

    int getColor() ;
}
