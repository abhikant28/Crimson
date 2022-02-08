package com.akw.crimson.AppObjects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_table")
public class Profile {

     @PrimaryKey
     private String ID;
     private String address;
     private String name;
     private String pic;
     private String phoneNumber;
     private String status;
     private String groups;

     public Profile(String address,String ID, String name, String pic, String phoneNumber, String status, String groups) {
          this.ID = ID;
          this.address=address;
          this.name = name;
          this.pic = pic;
          this.phoneNumber = phoneNumber;
          this.status = status;
          this.groups = groups;
     }

     public String getAddress() {
          return address;
     }

     public void setAddress(String address) {
          this.address = address;
     }

     public String getID() {
          return ID;
     }

     public void setID(String ID) {
          this.ID = ID;
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
}
