// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to add a scale ticket to EXTDSP
// Transaction AddScaleTktPur
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: PUNO - Purchase Order
 * @param: FACI - Facility
 * @param: DLDT - Delivery Date
 * @param: SUNO - Supplier
 * @param: YARD - Yard
 * @param: MSGN - Message Number
 * @param: INBN - Invoice Batch Number
*/


public class AddScaleTktPur extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI

  // Constructor 
  public AddScaleTktPur(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Contract Number
     int inCTNO  
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0        
     }
           
     // Purchase Order
     String inPUNO 
     if (mi.inData.get("PUNO") != null) {
        inPUNO = mi.inData.get("PUNO").trim() 
     } else {
        inPUNO = ""        
     }
 
     // Facility
     String inFACI  
     if (mi.inData.get("FACI") != null) {
        inFACI = mi.inData.get("FACI").trim() 
     } else {
        inFACI = ""        
     }

     // Delivery Date
     int inDLDT
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
     } else {
        inDLDT = 0        
     }

     // Supplier
     String inSUNO
     if (mi.inData.get("SUNO") != null) {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""        
     }

     // Yard
     String inYARD  
     if (mi.inData.get("YARD") != null) {
        inYARD = mi.inData.get("YARD").trim() 
     } else {
        inYARD = ""        
     }

     // Message Number
     String inMSGN  
     if (mi.inData.get("MSGN") != null) {
        inMSGN = mi.inData.get("MSGN").trim() 
     } else {
        inMSGN = ""        
     }

     // Invoice Batch Number
     int inINBN
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     } else {
        inINBN = 0      
     }

     // Species
     String inSPEC  
     if (mi.inData.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""        
     }

     // Logs
     int inLOGS
     if (mi.in.get("LOGS") != null) {
        inLOGS = mi.in.get("LOGS") 
     } else {
        inLOGS = 0      
     }

     // Amount
     double inAMNT
     if (mi.in.get("AMNT") != null) {
        inAMNT = mi.in.get("AMNT") 
     } else {
        inAMNT = 0d      
     }

     // Validate Scale Ticket record
     Optional<DBContainer> EXTDSP = findEXTDSP(inCONO, inDIVI, inSTID, inDLNO)
     if(EXTDSP.isPresent()){
        mi.error("Scale Ticket already exist")   
        return             
     } else {
        addEXTDSPRecord(inCONO, inDIVI, inSTID, inDLNO, inCTNO, inPUNO, inFACI, inDLDT, inSUNO, inYARD, inMSGN, inINBN, inSPEC, inLOGS, inAMNT)          
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
  // Add EXTDSP record 
  //********************************************************************     
  void addEXTDSPRecord(int CONO, String DIVI, int STID, int DLNO, int CTNO, String PUNO, String FACI, int DLDT, String SUNO, String YARD, String MSGN, int INBN, String SPEC, int LOGS, double AMNT){  
       DBAction action = database.table("EXTDSP").index("00").build()
       DBContainer EXTDSP = action.createContainer()
       EXTDSP.set("EXCONO", CONO)
       EXTDSP.set("EXDIVI", DIVI)
       EXTDSP.set("EXSTID", STID)
       EXTDSP.set("EXDLNO", DLNO)
       EXTDSP.set("EXCTNO", CTNO)
       EXTDSP.set("EXPUNO", PUNO)
       EXTDSP.set("EXFACI", FACI)
       EXTDSP.set("EXDLDT", DLDT)
       EXTDSP.set("EXSUNO", SUNO)
       EXTDSP.set("EXYARD", YARD)
       EXTDSP.set("EXMSGN", MSGN) 
       EXTDSP.set("EXINBN", INBN) 
       EXTDSP.set("EXSPEC", SPEC) 
       EXTDSP.set("EXLOGS", LOGS) 
       EXTDSP.set("EXAMNT", AMNT) 
       EXTDSP.set("EXCHID", program.getUser())
       EXTDSP.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDSP.set("EXRGDT", regdate) 
       EXTDSP.set("EXLMDT", regdate) 
       EXTDSP.set("EXRGTM", regtime)
       action.insert(EXTDSP)         
 } 
     
} 

