package pl.rozbijbank.db.model;

public interface User extends BasicModel{

      String getUserId();

      String getEmail();

      String getRecCode();

      String getAvatar();

      int getPoints();

      String getToken();

      boolean isEmailConfirmed() ;
}
