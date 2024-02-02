// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete delivery volume record from EXTDTV
// Transaction DelDeliveryVol
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

 public class DelDeliveryVol extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeliveryVol(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

     // Delivery Number
     int inDLNO   
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }


     // Validate delivery volume record
     Optional<DBContainer> EXTDTV = findEXTDTV(inCONO, inDIVI, inDLNO)
     if(!EXTDTV.isPresent()){
        mi.error("Delivery Volume record doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDTVRecord(inCONO, inDIVI, inDLNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDTV record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDTV(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDTV").index("00").build()
     DBContainer EXTDTV = query.getContainer()
     EXTDTV.set("EXCONO", CONO)
     EXTDTV.set("EXDIVI", DIVI)
     EXTDTV.set("EXDLNO", DLNO)
     if(query.read(EXTDTV))  { 
       return Optional.of(EXTDTV)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDTV
  //******************************************************************** 
  void deleteEXTDTVRecord(int CONO, String DIVI, int DLNO){ 
     DBAction action = database.table("EXTDTV").index("00").build()
     DBContainer EXTDTV = action.getContainer()
     EXTDTV.set("EXCONO", CONO)
     EXTDTV.set("EXDIVI", DIVI)
     EXTDTV.set("EXDLNO", DLNO)
     action.readLock(EXTDTV, deleterCallbackEXTDTV)
  }
    
  Closure<?> deleterCallbackEXTDTV = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


 }