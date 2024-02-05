// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete scale tickets from EXTDSP
// Transaction DelScaleTktPur
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: DLNO - Delivery Number
 * 
*/

 public class DelScaleTktPur extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelScaleTktPur(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

     // Scale Ticket ID
     int inSTID    
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0     
     }

     // Delivery Number
     int inDLNO     
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }


     // Validate scale ticket record
     Optional<DBContainer> EXTDSP = findEXTDSP(inCONO, inDIVI, inSTID, inDLNO)
     if(!EXTDSP.isPresent()){
        mi.error("Scale ticket doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDSPRecord(inCONO, inDIVI, inSTID, inDLNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDSP record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDSP(int CONO, String DIVI, int STID, int DLNO){  
     DBAction query = database.table("EXTDSP").index("00").build()
     DBContainer EXTDSP = query.getContainer()
     EXTDSP.set("EXCONO", CONO)
     EXTDSP.set("EXDIVI", DIVI)
     EXTDSP.set("EXSTID", STID)
     EXTDSP.set("EXDLNO", DLNO)
     if(query.read(EXTDSP))  { 
       return Optional.of(EXTDSP)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDSP
  //******************************************************************** 
  void deleteEXTDSPRecord(int CONO, String DIVI, int STID, int DLNO){ 
     DBAction action = database.table("EXTDSP").index("00").build()
     DBContainer EXTDSP = action.getContainer()
     EXTDSP.set("EXCONO", CONO)
     EXTDSP.set("EXDIVI", DIVI)
     EXTDSP.set("EXSTID", STID)
     EXTDSP.set("EXDLNO", DLNO)
     action.readLock(EXTDSP, deleterCallbackEXTDSP)
  }
    
  Closure<?> deleterCallbackEXTDSP = { LockedResult lockedResult ->  
     lockedResult.delete()
  }



 }