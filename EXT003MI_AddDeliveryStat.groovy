// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a delivery status to EXTDLS
// Transaction AddDeliveryStat
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STAT - Status
 * @param: MDUL - Module
 * @param: CRDT - Date Updated
 * @param: USID - Updated By
 * @param: NOTE - Note
*/


public class AddDeliveryStat extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Definition 
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddDeliveryStat(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
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
     
     // Sequence
     int inSEQN
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }

     // Status
     int inSTAT
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0        
     }

     // Module
     String inMDUL
     if (mi.in.get("MDUL") != null) {
        inMDUL = mi.in.get("MDUL") 
     } else {
        inMDUL = ""       
     }

     // Date Updated
     int inCRDT
     if (mi.in.get("CRDT") != null) {
        inCRDT = mi.in.get("CRDT") 
     } else {
        inCRDT = 0        
     }

     // Updated By
     String inUSID
     if (mi.in.get("USID") != null) {
        inUSID = mi.in.get("USID") 
     } else {
        inUSID = ""       
     }

     // Comment
     String inNOTE
     if (mi.in.get("NOTE") != null) {
        inNOTE = mi.in.get("NOTE") 
     } else {
        inNOTE = ""       
     }


     // Validate Delivery Status record
     Optional<DBContainer> EXTDLS = findEXTDLS(inCONO, inDIVI, inDLNO, inSEQN)
     if(EXTDLS.isPresent()){
        mi.error("Delivery Status already exist")   
        return             
     } else {
        // Write record 
        addEXTDLSRecord(inCONO, inDIVI, inDLNO, inSEQN, inSTAT, inMDUL, inCRDT, inUSID, inNOTE)          
     }  
     
  }
  

  //******************************************************************** 
  // Get EXTDLS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLS(int CONO, String DIVI, int DLNO, int SEQN){  
     DBAction query = database.table("EXTDLS").index("00").build()
     DBContainer EXTDLS = query.getContainer()
     EXTDLS.set("EXCONO", CONO)
     EXTDLS.set("EXDIVI", DIVI)
     EXTDLS.set("EXDLNO", DLNO)
     EXTDLS.set("EXSEQN", SEQN)
     if(query.read(EXTDLS))  { 
       return Optional.of(EXTDLS)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDLS record 
  //********************************************************************     
  void addEXTDLSRecord(int CONO, String DIVI, int DLNO, int SEQN, int STAT, String MDUL, int CRDT, String USID, String NOTE){  
       DBAction action = database.table("EXTDLS").index("00").build()
       DBContainer EXTDLS = action.createContainer()
       EXTDLS.set("EXCONO", CONO)
       EXTDLS.set("EXDIVI", DIVI)
       EXTDLS.set("EXDLNO", DLNO)
       EXTDLS.set("EXSEQN", SEQN)
       EXTDLS.set("EXSTAT", STAT)
       EXTDLS.set("EXMDUL", MDUL)
       EXTDLS.set("EXCRDT", CRDT)
       EXTDLS.set("EXUSID", USID)
       EXTDLS.set("EXNOTE", NOTE)   
       EXTDLS.set("EXCHID", program.getUser())
       EXTDLS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDLS.set("EXRGDT", regdate) 
       EXTDLS.set("EXLMDT", regdate) 
       EXTDLS.set("EXRGTM", regtime)
       action.insert(EXTDLS)         
 } 

     
} 

