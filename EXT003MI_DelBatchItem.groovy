// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete batch item record from EXTDBI
// Transaction DelBatchItem
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: SUNO - Supplier
 * @param: ITNO - Item Number
*/

 public class DelBatchItem extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelBatchItem(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database
     this.program = program
     this.logger = logger
     this.miCaller = miCaller
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
     int inDBNO   
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO") 
     } else {
        inDBNO = 0     
     }

     // Supplier
     String inSUNO  
     if (mi.inData.get("SUNO") != null) {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""     
     }


     // Validate batch item record
     Optional<DBContainer> EXTDBI = findEXTDBI(inCONO, inDIVI, inDBNO, inSUNO)
     if(!EXTDBI.isPresent()){
        mi.error("Batch Item record doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDBIRecord(inCONO, inDIVI, inDBNO, inSUNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDBI record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBI(int CONO, String DIVI, int DBNO, String SUNO){  
     DBAction query = database.table("EXTDBI").index("00").build()
     DBContainer EXTDBI = query.getContainer()
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
  // Delete record from EXTDBI
  //******************************************************************** 
  void deleteEXTDBIRecord(int CONO, String DIVI, int DBNO, String SUNO){ 
     DBAction action = database.table("EXTDBI").index("00").build()
     DBContainer EXTDBI = action.getContainer()
     EXTDBI.set("EXCONO", CONO)
     EXTDBI.set("EXDIVI", DIVI)
     EXTDBI.set("EXDBNO", DBNO)
     EXTDBI.set("EXSUNO", SUNO)
     action.readLock(EXTDBI, deleterCallbackEXTDBI)
  }
    
  Closure<?> deleterCallbackEXTDBI = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


 }