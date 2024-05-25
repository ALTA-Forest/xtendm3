// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to update scale ticket in EXTDSP
// Transaction UpdScaleTktPur
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: DLNO - Delivery Number
*/


public class UpdScaleTktPur extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTID
  int inCTNO
  String inTREF
  String inFACI
  int inDLDT
  String inSUNO
  String inYARD
  String inMSGN
  int inINBN
  String inSPEC
  int inLOGS
  double inAMNT
  
  // Constructor 
  public UpdScaleTktPur(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0        
     }

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0         
     }

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } 
           
     // Reference
     if (mi.in.get("TREF") != null && mi.in.get("TREF") != "") {
        inTREF = mi.inData.get("TREF").trim() 
     } 
 
     // Facility
     String inFACI  
     if (mi.in.get("FACI") != null && mi.in.get("FACI") != "") {
        inFACI = mi.inData.get("FACI").trim() 
        
        // Validate facility if entered
        Optional<DBContainer> CFACIL = findCFACIL(inCONO, inFACI)
        if (!CFACIL.isPresent()) {
           mi.error("Facility doesn't exist")   
           return             
        }

     } 

     // Delivery Date
     int inDLDT
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
        
        //Validate date format
        boolean validDLDT = utility.call("DateUtil", "isDateValid", String.valueOf(inDLDT), "yyyyMMdd")  
        if (!validDLDT) {
           mi.error("Delivery Date is not valid")   
           return  
        } 

     } 

     // Supplier
     String inSUNO
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
        
        // Validate supplier if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
        if (!CIDMAS.isPresent()) {
           mi.error("Supplier doesn't exist")   
           return             
        }

     }

     // Yard
     String inYARD  
     if (mi.in.get("YARD") != null && mi.in.get("YARD") != "") {
        inYARD = mi.inData.get("YARD").trim() 
     } 

     // Message Number
     String inMSGN  
     if (mi.in.get("MSGN") != null && mi.in.get("MSGN") != "") {
        inMSGN = mi.inData.get("MSGN").trim() 
     } 

     // Invoice Batch Number
     int inINBN
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     } 
     
     // Species
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""        
     }

     // Logs
     if (mi.in.get("LOGS") != null) {
        inLOGS = mi.in.get("LOGS") 
     } else {
        inLOGS = 0      
     }

     // Amount
     if (mi.in.get("AMNT") != null) {
        inAMNT = mi.in.get("AMNT") 
     } else {
        inAMNT = 0d      
     }


     // Validate Scale Ticket record
     Optional<DBContainer> EXTDSP = findEXTDSP(inCONO, inDIVI, inSTID, inDLNO)
     if (!EXTDSP.isPresent()) {
        mi.error("Scale Ticket record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDSPRecord()
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
   // Check Facility
   //******************************************************************** 
   private Optional<DBContainer> findCFACIL(int CONO, String FACI){  
     DBAction query = database.table("CFACIL").index("00").build()   
     DBContainer CFACIL = query.getContainer()
     CFACIL.set("CFCONO", CONO)
     CFACIL.set("CFFACI", FACI)
    
     if(query.read(CFACIL))  { 
       return Optional.of(CFACIL)
     } 
  
     return Optional.empty()
   }


   //******************************************************************** 
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }


  //******************************************************************** 
  // Update EXTDSP record
  //********************************************************************    
  void updEXTDSPRecord(){      
     DBAction action = database.table("EXTDSP").index("00").build()
     DBContainer EXTDSP = action.getContainer()     
     EXTDSP.set("EXCONO", inCONO)
     EXTDSP.set("EXDIVI", inDIVI)
     EXTDSP.set("EXSTID", inSTID)
     EXTDSP.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDSP, updateCallBackEXTDSP)
     }
   
     Closure<?> updateCallBackEXTDSP = { LockedResult lockedResult ->      
     if (mi.in.get("CTNO") != null) {
        lockedResult.set("EXCTNO", mi.in.get("CTNO"))
     }

     if (mi.in.get("TREF") != null) {
        lockedResult.set("EXTREF", mi.in.get("TREF"))
     }

     if (mi.in.get("FACI") != null) {
        lockedResult.set("EXFACI", mi.in.get("FACI"))
     }

     if (mi.in.get("DLDT") != null) {
        lockedResult.set("EXDLDT", mi.in.get("DLDT"))
     }

     if (mi.in.get("SUNO") != null) {
        lockedResult.set("EXSUNO", mi.in.get("SUNO"))
     }

     if (mi.in.get("YARD") != null) {
        lockedResult.set("EXYARD", mi.in.get("YARD"))
     }

     if (mi.in.get("MSGN") != null) {
        lockedResult.set("EXMSGN", mi.in.get("MSGN"))
     }

     if (mi.in.get("INBN") != null) {
        lockedResult.set("EXINBN", mi.in.get("INBN"))
     }
     
     if (mi.in.get("SPEC") != null) {
        lockedResult.set("EXSPEC", mi.in.get("SPEC"))
     }

     if (mi.in.get("LOGS") != null) {
        lockedResult.set("EXLOGS", mi.in.get("LOGS"))
     }

     if (mi.in.get("AMNT") != null) {
        lockedResult.set("EXAMNT", mi.in.get("AMNT"))
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

