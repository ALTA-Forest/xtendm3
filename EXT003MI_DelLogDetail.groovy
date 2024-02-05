// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete log detail record from EXTSLD
// Transaction DelLogDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: LDID - Log Detail ID
 * 
*/


 public class DelLogDetail extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelLogDetail(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
     
     // Scale Ticket ID
     int inSTID     
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0     
     }

     // Log Detail ID
     int inLDID     
     if (mi.in.get("LDID") != null) {
        inLDID = mi.in.get("LDID") 
     } else {
        inLDID = 0     
     }



     // Validate log detail record
     Optional<DBContainer> EXTSLD = findEXTSLD(inCONO, inDIVI, inSTID, inLDID)
     if(!EXTSLD.isPresent()){
        mi.error("Log Detail doesn't exist")   
        return             
     } else {
        // Delete record
        deleteEXTSLDRecord(inCONO, inDIVI, inSTID, inLDID) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTSLD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSLD(int CONO, String DIVI, int STID, int LDID){  
     DBAction query = database.table("EXTSLD").index("00").build()
     DBContainer EXTSLD = query.getContainer()
     EXTSLD.set("EXCONO", CONO)
     EXTSLD.set("EXDIVI", DIVI)
     EXTSLD.set("EXSTID", STID)
     EXTSLD.set("EXLDID", LDID)
     if(query.read(EXTSLD))  { 
       return Optional.of(EXTSLD)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTSLD
  //******************************************************************** 
  void deleteEXTSLDRecord(int CONO, String DIVI, int STID, int LDID){ 
     DBAction action = database.table("EXTSLD").index("00").build()
     DBContainer EXTSLD = action.getContainer()
     EXTSLD.set("EXCONO", CONO)
     EXTSLD.set("EXDIVI", DIVI)
     EXTSLD.set("EXSTID", STID)
     EXTSLD.set("EXLDID", LDID)
     action.readLock(EXTSLD, deleterCallbackEXTSLD)
  }
    
  Closure<?> deleterCallbackEXTSLD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }