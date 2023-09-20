// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a supplier truck from EXTSTR
// Transaction DelSupTruck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * 
*/

 import java.time.LocalDateTime;  
 import java.time.format.DateTimeFormatter;

 public class DelSupTruck extends ExtendM3Transaction {
    private final MIAPI mi; 
    private final DatabaseAPI database; 
    private final ProgramAPI program;
    private final LoggerAPI logger;
  
    int CONO
    String inSUNO
    String inTRCK
    
  // Constructor 
  public DelSupTruck(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
     // Set Company Number
     CONO = program.LDAZD.CONO as Integer

     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""     
     }

     // Truck
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""     
     }

     // Validate supplier truck record
     Optional<DBContainer> EXTSTR = findEXTSTR(CONO, inSUNO, inTRCK)
     if(!EXTSTR.isPresent()){
        mi.error("Supplier Truck doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTSTRRecord() 
     } 
     
  }

  //******************************************************************** 
  // Validate String
  //******************************************************************** 
  public  boolean isNullOrEmpty(String key) {
      if(key != null && !key.isEmpty())
         return false;
      return true;
  }
    

  //******************************************************************** 
  // Get EXTSTR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSTR(int CONO, String SUNO, String TRCK){  
     DBAction query = database.table("EXTSTR").index("00").selection("EXCONO", "EXSUNO", "EXTRCK").build()
     def EXTSTR = query.getContainer()
     EXTSTR.set("EXCONO", CONO)
     EXTSTR.set("EXSUNO", SUNO)
     EXTSTR.set("EXTRCK", TRCK)
     if(query.read(EXTSTR))  { 
       return Optional.of(EXTSTR)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTSTR
  //******************************************************************** 
  void deleteEXTSTRRecord(){ 

     DBAction action = database.table("EXTSTR").index("00").selectAllFields().build()
     DBContainer EXTSTR = action.getContainer()
     EXTSTR.set("EXCONO", CONO) 
     EXTSTR.set("EXSUNO", inSUNO)
     EXTSTR.set("EXTRCK", inTRCK)

     action.readLock(EXTSTR, deleterCallbackEXTSTR)
  }
    
  Closure<?> deleterCallbackEXTSTR = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }