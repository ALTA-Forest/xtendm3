// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update a contract instruction in EXTDSL
// Transaction UpdScaleTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket Line
 * @param: PONR - Line Number
 * @param: ITNO - Item Number
 * @param: ORQT - Quantity
 * @param: STAM - Share Amount
*/



public class UpdScaleTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inPONR
  String inITNO
  double inORQT
  double inSTAM  
  
  // Constructor 
  public UpdScaleTktLn(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }
           
     // Line Number
     if (mi.in.get("PONR") != null) {
        inPONR = mi.in.get("PONR") 
     } else {
        inPONR = 0        
     }
     
     // Item Number
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""        
     }

     // Quantity
     if (mi.in.get("ORQT") != null) {
        inORQT = mi.in.get("ORQT") 
     } else {
        inORQT = 0        
     }

     // Share Amount
     if (mi.in.get("STAM") != null) {
        inSTAM = mi.in.get("STAM") 
     } else {
        inSTAM = 0       
     }


     // Validate Scale Ticket Line record
     Optional<DBContainer> EXTDSL = findEXTDSL(inCONO, inDIVI, inSTID, inPONR, inITNO)
     if (!EXTDSL.isPresent()) {
        mi.error("Scale Ticket Line doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDSLRecord()
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
  // Update EXTDSL record
  //********************************************************************    
  void updEXTDSLRecord(){      
     DBAction action = database.table("EXTDSL").index("00").build()
     DBContainer EXTDSL = action.getContainer()     
     EXTDSL.set("EXCONO", inCONO)
     EXTDSL.set("EXDIVI", inDIVI)
     EXTDSL.set("EXSTID", inSTID)
     EXTDSL.set("EXPONR", inPONR)
     EXTDSL.set("EXITNO", inITNO)

     // Read with lock
     action.readLock(EXTDSL, updateCallBackEXTDSL)
     }
   
     Closure<?> updateCallBackEXTDSL = { LockedResult lockedResult -> 
       if (inORQT != 0) {
          lockedResult.set("EXORQT", inORQT)
       }
  
       if (inSTAM != 0) {
          lockedResult.set("EXSTAM", inSTAM)
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

