package pl.rozbijbank.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import pl.rozbijbank.db.model.User;
import pl.rozbijbank.networking.pojo.session.UserPojo;

@Entity(tableName = "table_active_users")
public class UserEntity extends BasicEntity implements User {

    private String userId;
    private String email;
    @ColumnInfo(name = "recommendation_code")
    private String recCode;
    private String avatar;
    private int points;
    private String token;
    @ColumnInfo(name = "email_confirmed")
    private boolean emailConfirmed;

    public UserEntity() {
    }

    @Ignore
    public UserEntity(UserPojo userPojo) {
        this.userId = userPojo.getUserId();
        this.email = userPojo.getEmail();
        this.recCode = userPojo.getRecommendationCode();
        this.avatar = userPojo.getAvatar();
        this.points = userPojo.getPoints();
        this.token = userPojo.getToken();
        this.emailConfirmed= userPojo.isEmailConfirmed();
    }

    @Ignore
    public UserEntity(String email, String recCode, String avatar, int points, String token, boolean emailConfirmed) {
        this.email = email;
        this.recCode = recCode;
        this.avatar = avatar;
        this.points = points;
        this.token = token;
        this.emailConfirmed=emailConfirmed;
    }

    //getter
    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getRecCode() {
        return recCode;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

//setter

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRecCode(String recCode) {
        this.recCode = recCode;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }
}
