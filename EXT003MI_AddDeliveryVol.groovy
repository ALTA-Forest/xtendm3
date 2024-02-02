// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to add a delivery volume to EXTDTV
// Transaction AddDeliveryVol
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: WTNO - Weight Ticket ID
 * @param: DLNO - Delivery Number
 * @param: GRWE - Gross Weight
 * @param: TRWE - Tare Weight
 * @param: NEWE - Net Weight
 * @param: AMNT - Amount
*/


public class AddDeliveryVol extends ExtendM3Transaction {
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
  public AddDeliveryVol(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
     
     // Load
     int inLOAD
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } else {
        inLOAD = 0d         
     }

     // Log
     int inVLOG
     if (mi.in.get("VLOG") != null) {
        inVLOG = mi.in.get("VLOG") 
     } else {
        inVLOG = 0d         
     }
   
     // Gross Volume
     double inGVBF
     if (mi.in.get("GVBF") != null) {
        inGVBF= mi.in.get("GVBF") 
     } else {
        inGVBF = 0d         
     }
        
     // Net Volume
     double inNVBF
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } else {
        inNVBF = 0d         
     }
     
     // Amount
     double inAMNT
     if (mi.in.get("AMNT") != null) {
        inAMNT = mi.in.get("AMNT") 
     } else {
        inAMNT = 0d         
     }



     // Validate Delivery Volume record
     Optional<DBContainer> EXTDTV = findEXTDTV(inCONO, inDIVI, inDLNO)
     if(EXTDTV.isPresent()){
        mi.error("Delivery Volume already exist")   
        return             
     } else {
        // Write record 
        addEXTDTVRecord(inCONO, inDIVI, inDLNO, inLOAD, inVLOG, inGVBF, inNVBF, inAMNT)         
     }  
     
  }
  

  //******************************************************************** 
  // Get EXTDTV record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDTV(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDTV").index("00").build()
     def EXTDTV = query.getContainer()
     EXTDTV.set("EXCONO", CONO)
     EXTDTV.set("EXDIVI", DIVI)
     EXTDTV.set("EXDLNO", DLNO)
     if(query.read(EXTDTV))  { 
       return Optional.of(EXTDTV)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDTV record 
  //********************************************************************     
  void addEXTDTVRecord(int CONO, String DIVI, int DLNO, double LOAD, double VLOG, double GVBF, double NVBF, double AMNT){  
       DBAction action = database.table("EXTDTV").index("00").build()
       DBContainer EXTDTV = action.createContainer()
       EXTDTV.set("EXCONO", CONO)
       EXTDTV.set("EXDIVI", DIVI)
       EXTDTV.set("EXDLNO", DLNO)
       EXTDTV.set("EXLOAD", LOAD)
       EXTDTV.set("EXVLOG", VLOG)
       EXTDTV.set("EXGVBF", GVBF)
       EXTDTV.set("EXNVBF", NVBF)
       EXTDTV.set("EXAMNT", AMNT)
       EXTDTV.set("EXCHID", program.getUser())
       EXTDTV.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDTV.set("EXRGDT", regdate) 
       EXTDTV.set("EXLMDT", regdate) 
       EXTDTV.set("EXRGTM", regtime)
       action.insert(EXTDTV)         
 } 

     
} 

