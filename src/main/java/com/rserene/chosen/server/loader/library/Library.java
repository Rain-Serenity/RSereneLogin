package com.rserene.chosen.server.loader.library;

import lombok.Generated;

public class Library {
   private final String group;
   private final String name;
   private final String version;

   public static Library of(String value, String split) {
      String[] args = value.split(split);
      return new Library(args[0], args[1], args[2]);
   }

   public String getFileName() {
      return String.format("%s-%s.jar", this.name, this.version);
   }

   public String getDownloadUrl() {
      String var10000 = this.group.replace(".", "/");
      return var10000 + "/" + this.name + "/" + this.version + "/" + this.name + "-" + this.version + ".jar";
   }

   @Generated
   public Library(String group, String name, String version) {
      this.group = group;
      this.name = name;
      this.version = version;
   }

   @Generated
   public String getGroup() {
      return this.group;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getVersion() {
      return this.version;
   }

   @Generated
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Library)) {
         return false;
      } else {
         Library other = (Library)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$group = this.getGroup();
            Object other$group = other.getGroup();
            if (this$group == null) {
               if (other$group != null) {
                  return false;
               }
            } else if (!this$group.equals(other$group)) {
               return false;
            }

            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
               if (other$name != null) {
                  return false;
               }
            } else if (!this$name.equals(other$name)) {
               return false;
            }

            Object this$version = this.getVersion();
            Object other$version = other.getVersion();
            if (this$version == null) {
               if (other$version != null) {
                  return false;
               }
            } else if (!this$version.equals(other$version)) {
               return false;
            }

            return true;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Library;
   }

   @Generated
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $group = this.getGroup();
      result = result * 59 + ($group == null ? 43 : $group.hashCode());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $version = this.getVersion();
      result = result * 59 + ($version == null ? 43 : $version.hashCode());
      return result;
   }

   @Generated
   public String toString() {
      String var10000 = this.getGroup();
      return "Library(group=" + var10000 + ", name=" + this.getName() + ", version=" + this.getVersion() + ")";
   }
}
