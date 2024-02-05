// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete a contract status from EXTDSL
// Transaction DelScaleTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: PONR - Line Number
 * @param: ITNO - Item Number
 * 
*/


 public class DelScaleTktLn extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelScaleTktLn(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
     
     // Line Number
     int inPONR      
     if (mi.in.get("PONR") != null) {
        inPONR = mi.in.get("PONR") 
     } else {
        inPONR = 0     
     }

     // Item Number
     String inITNO      
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""     
     }

     // Validate Scale Ticket Line record
     Optional<DBContainer> EXTDSL = findEXTDSL(inCONO, inDIVI, inSTID, inPONR, inITNO)
     if(!EXTDSL.isPresent()){
        mi.error("Scale Ticket Line doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDSLRecord(inCONO, inDIVI, inSTID, inPONR, inITNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDSL record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDSL(int CONO, String DIVI, int STID, int PONR, String ITNO){  
     DBAction query = database.table("EXTDSL").index("00").build()
     DBContainer EXTDSL = query.getContainer()
     EXTDSL.set("EXCONO", CONO)
     EXTDSL.set("EXDIVI", DIVI)
     EXTDSL.set("EXSTID", STID)
     EXTDSL.set("EXPONR", PONR)
     EXTDSL.set("EXITNO", ITNO)
     if(query.read(EXTDSL))  { 
       return Optional.of(EXTDSL)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDSL
  //******************************************************************** 
  void deleteEXTDSLRecord(int CONO, String DIVI, int STID, int PONR, String ITNO){ 
     DBAction action = database.table("EXTDSL").index("00").build()
     DBContainer EXTDSL = action.getContainer()
     EXTDSL.set("EXCONO", CONO)
     EXTDSL.set("EXDIVI", DIVI)
     EXTDSL.set("EXSTID", STID)
     EXTDSL.set("EXPONR", PONR)
     EXTDSL.set("EXITNO", ITNO)

     action.readLock(EXTDSL, deleterCallbackEXTDSL)
  }
    
  Closure<?> deleterCallbackEXTDSL = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }