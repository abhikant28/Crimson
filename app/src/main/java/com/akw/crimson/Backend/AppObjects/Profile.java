package com.akw.crimson.Backend.AppObjects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_table")
public class Profile {

     @PrimaryKey
     @NonNull private String user_ID;
     private String address,name,pic,phoneNumber,status,groups;
     private boolean blocked;

     public Profile(String address,String user_ID, String name, String pic, String phoneNumber, String status, String groups, boolean blocked) {
          this.user_ID = user_ID;
          this.address=address;
          this.name = name;
          this.pic = pic;
          this.phoneNumber = phoneNumber;
          this.status = status;
          this.groups = groups;
          this.blocked=blocked;
     }

     public String getUser_ID() {
          return user_ID;
     }

     public void setUser_ID(String user_ID) {
          this.user_ID = user_ID;
     }

     public String getAddress() {
          return address;
     }

     public void setAddress(String address) {
          this.address = address;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getPic() {
          return pic;
     }

     public void setPic(String pic) {
          this.pic = pic;
     }

     public String getPhoneNumber() {
          return phoneNumber;
     }

     public void setPhoneNumber(String phoneNumber) {
          this.phoneNumber = phoneNumber;
     }

     public String getStatus() {
          return status;
     }

     public void setStatus(String status) {
          this.status = status;
     }

     public String getGroups() {
          return groups;
     }

     public void setGroups(String groups) {
          this.groups = groups;
     }

     public boolean isBlocked() {
          return blocked;
     }

     public void setBlocked(boolean blocked) {
          this.blocked = blocked;
     }
}
