// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete batch details from EXTDBD
// Transaction DelBatchDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: DLNO - Delivery Number
 * @param: SUNO - Supplier
 * @param: ITNO - Item Number
*/

 public class DelBatchDetail extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelBatchDetail(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
     inDIVI = mi.in.get("DIVI")
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

     // Delivery Number
     int inDLNO   
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Supplier
     String inSUNO  
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""     
     }

     // Item Number
     String inITNO   
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""     
     }


     // Validate batch detail record
     Optional<DBContainer> EXTDBD = findEXTDBD(inCONO, inDIVI, inDBNO, inDLNO, inSUNO, inITNO)
     if(!EXTDBD.isPresent()){
        mi.error("Batch Details doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDBDRecord(inCONO, inDIVI, inDBNO, inDLNO, inSUNO, inITNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDBD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBD(int CONO, String DIVI, int DBNO, int DLNO, String SUNO, String ITNO){  
     DBAction query = database.table("EXTDBD").index("00").build()
     DBContainer EXTDBD = query.getContainer()
     EXTDBD.set("EXCONO", CONO)
     EXTDBD.set("EXDIVI", DIVI)
     EXTDBD.set("EXDBNO", DBNO)
     EXTDBD.set("EXDLNO", DLNO)
     EXTDBD.set("EXSUNO", SUNO)
     EXTDBD.set("EXITNO", ITNO)
     if(query.read(EXTDBD))  { 
       return Optional.of(EXTDBD)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDBD
  //******************************************************************** 
  void deleteEXTDBDRecord(int CONO, String DIVI, int DBNO, int DLNO, String SUNO, String ITNO){ 
     DBAction action = database.table("EXTDBD").index("00").build()
     DBContainer EXTDBD = action.getContainer()
     EXTDBD.set("EXCONO", CONO)
     EXTDBD.set("EXDIVI", DIVI)
     EXTDBD.set("EXDBNO", DBNO)
     EXTDBD.set("EXDLNO", DLNO)
     EXTDBD.set("EXSUNO", SUNO)
     EXTDBD.set("EXITNO", ITNO)
     action.readLock(EXTDBD, deleterCallbackEXTDBD)
  }
    
  Closure<?> deleterCallbackEXTDBD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


 }