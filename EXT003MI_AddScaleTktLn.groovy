// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a scale ticket line to EXTDSL
// Transaction AddScaleTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket Line
 * @param: PONR - Line Number
 * @param: ITNO - Item Number
 * @param: EOQT - Gross Quantity
 * @param: ORQT - Quantity
 * @param: STAM - Share Amount
*/


public class AddScaleTktLn extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  // Definition 
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddScaleTktLn(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     
     // Gross Quantity
     double inEOQT
     if (mi.in.get("EOQT") != null) {
        inEOQT = mi.in.get("EOQT") 
     } else {
        inEOQT = 0d        
     }

     // Quantity
     double inORQT
     if (mi.in.get("ORQT") != null) {
        inORQT = mi.in.get("ORQT") 
     } else {
        inORQT = 0d        
     }

     // Share Amount
     double inSTAM
     if (mi.in.get("STAM") != null) {
        inSTAM = mi.in.get("STAM") 
     } else {
        inSTAM = 0d       
     }

     // Validate Scale Ticket Line record
     Optional<DBContainer> EXTDSL = findEXTDSL(inCONO, inDIVI, inSTID, inPONR, inITNO)
     if(EXTDSL.isPresent()){
        mi.error("Scale Ticket Line already exist")   
        return             
     } else {
        // Write record 
        addEXTDSLRecord(inCONO, inDIVI, inSTID, inPONR, inITNO, inEOQT, inORQT, inSTAM)          
     }  
     
  }
  

  //******************************************************************** 
  // Get EXTDSL record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDSL(int CONO, String DIVI, int STID, int PONR, String ITNO){  
     DBAction query = database.table("EXTDSL").index("00").build()
     def EXTDSL = query.getContainer()
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
  // Add EXTDSL record 
  //********************************************************************     
  void addEXTDSLRecord(int CONO, String DIVI, int STID, int PONR, String ITNO, double EOQT, double ORQT, double STAM){  
       DBAction action = database.table("EXTDSL").index("00").build()
       DBContainer EXTDSL = action.createContainer()
       EXTDSL.set("EXCONO", CONO)
       EXTDSL.set("EXDIVI", DIVI)
       EXTDSL.set("EXSTID", STID)
       EXTDSL.set("EXPONR", PONR)
       EXTDSL.set("EXITNO", ITNO)
       EXTDSL.set("EXEOQT", EOQT)
       EXTDSL.set("EXORQT", ORQT)
       EXTDSL.set("EXSTAM", STAM)  
       EXTDSL.set("EXCHID", program.getUser())
       EXTDSL.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDSL.set("EXRGDT", regdate) 
       EXTDSL.set("EXLMDT", regdate) 
       EXTDSL.set("EXRGTM", regtime)
       action.insert(EXTDSL)         
 } 
     
} 

