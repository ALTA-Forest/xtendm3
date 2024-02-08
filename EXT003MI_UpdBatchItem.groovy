// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to update a batch item record in EXTDBI
// Transaction UpdBatchItem
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: SUNO - Supplier
 * @param: BIAM - Amount
*/


public class UpdBatchItem extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDBNO
  String inSUNO
  double inBIAM
  
  // Constructor 
  public UpdBatchItem(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger  
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Batch Number
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO") 
     } else {
        inDBNO = 0         
     }

     // Supplier
     if (mi.inData.get("SUNO") != null) {
        inSUNO = mi.inData.get("SUNO").trim()
     } else {
        inSUNO = ""        
     }
 
     // Amount
     if (mi.in.get("BIAM") != null) {
        inBIAM = mi.in.get("BIAM") 
     } 

     // Validate Batch record
     Optional<DBContainer> EXTDBI = findEXTDBI(inCONO, inDIVI, inDBNO, inSUNO)
     if (!EXTDBI.isPresent()) {
        mi.error("Batch record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDBIRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDBI record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBI(int CONO, String DIVI, int DBNO, String SUNO){  
     DBAction query = database.table("EXTDBI").index("00").build()
     def EXTDBI = query.getContainer()
     EXTDBI.set("EXCONO", CONO)
     EXTDBI.set("EXDIVI", DIVI)
     EXTDBI.set("EXDBNO", DBNO)
     EXTDBI.set("EXSUNO", SUNO)
     if(query.read(EXTDBI))  { 
       return Optional.of(EXTDBI)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTDBI record
  //********************************************************************    
  void updEXTDBIRecord(){      
     DBAction action = database.table("EXTDBI").index("00").build()
     DBContainer EXTDBI = action.getContainer()     
     EXTDBI.set("EXCONO", inCONO)
     EXTDBI.set("EXDIVI", inDIVI)
     EXTDBI.set("EXDBNO", inDBNO)
     EXTDBI.set("EXSUNO", inSUNO)

     // Read with lock
     action.readLock(EXTDBI, updateCallBackEXTDBI)
     }
   
     Closure<?> updateCallBackEXTDBI = { LockedResult lockedResult ->      
     if (mi.in.get("BIAM") != null) {
        lockedResult.set("EXBIAM", mi.in.get("BIAM"))
     }
     
     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     int changedate = utility.call("DateUtil", "currentDateY8AsInt")
     lockedResult.set("EXLMDT", changedate)        
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }

} 

