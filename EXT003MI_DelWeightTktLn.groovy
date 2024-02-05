// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete a weight ticket line from EXTDWT
// Transaction DelWeightTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: WTNO - Weight Ticket ID
 * @param: DLNO - Delivery Number
 * 
*/


 public class DelWeightTktLn extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelWeightTktLn(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Weight Ticket ID
     int inWTNO      
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0     
     }
     
     // Delivery Number
     int inDLNO     
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }


     // Validate Weight Ticket Line record
     Optional<DBContainer> EXTDWT = findEXTDWT(inCONO, inDIVI, inWTNO, inDLNO)
     if(!EXTDWT.isPresent()){
        mi.error("Weight Ticket Line doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDWTRecord(inCONO, inDIVI, inWTNO, inDLNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDWT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDWT(int CONO, String DIVI, int WTNO, int DLNO){  
     DBAction query = database.table("EXTDWT").index("00").build()
     DBContainer EXTDWT = query.getContainer()
     EXTDWT.set("EXCONO", CONO)
     EXTDWT.set("EXDIVI", DIVI)
     EXTDWT.set("EXWTNO", WTNO)
     EXTDWT.set("EXDLNO", DLNO)
     if(query.read(EXTDWT))  { 
       return Optional.of(EXTDWT)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDWT
  //******************************************************************** 
  void deleteEXTDWTRecord(int CONO, String DIVI, int WTNO, int DLNO){ 
     DBAction action = database.table("EXTDWT").index("00").build()
     DBContainer EXTDWT = action.getContainer()
     EXTDWT.set("EXCONO", CONO)
     EXTDWT.set("EXDIVI", DIVI)
     EXTDWT.set("EXWTNO", WTNO)
     EXTDWT.set("EXDLNO", DLNO)

     action.readLock(EXTDWT, deleterCallbackEXTDWT)
  }
    
  Closure<?> deleterCallbackEXTDWT = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }