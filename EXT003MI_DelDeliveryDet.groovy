// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete a contract status from EXTDLH
// Transaction DelDelivery
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * 
*/


 public class DelDeliveryDet extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeliveryDet(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Delivery Number
     int inDLNO      
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }
     
     // Validate Delivery Detail record
     Optional<DBContainer> EXTDLD = findEXTDLD(inCONO, inDIVI, inDLNO)
     if(!EXTDLD.isPresent()){
        mi.error("Delivery details don't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTDLDRecord(inCONO, inDIVI, inDLNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDLD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLD(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLD").index("00").build()
     DBContainer EXTDLD = query.getContainer()
     EXTDLD.set("EXCONO", CONO)
     EXTDLD.set("EXDIVI", DIVI)
     EXTDLD.set("EXDLNO", DLNO)
     if(query.read(EXTDLD))  { 
       return Optional.of(EXTDLD)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDLD
  //******************************************************************** 
  void deleteEXTDLDRecord(int CONO, String DIVI, int DLNO){ 
     DBAction action = database.table("EXTDLD").index("00").build()
     DBContainer EXTDLD = action.getContainer()
     EXTDLD.set("EXCONO", CONO)
     EXTDLD.set("EXDIVI", DIVI)
     EXTDLD.set("EXDLNO", DLNO)

     action.readLock(EXTDLD, deleterCallbackEXTDLD)
  }
    
  Closure<?> deleterCallbackEXTDLD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }