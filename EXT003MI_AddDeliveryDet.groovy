// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a delivery details to EXTDLD
// Transaction AddDeliveryDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: WTNO - Weight Ticket Number
 * @param: STNO - Scale Ticket Number
*/


public class AddDeliveryDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Definition 
  Integer inCONO
  String inDIVI
  int inCTNO

  
  // Constructor 
  public AddDeliveryDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
 
     // Contract Number
     int inCTNO
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0         
     }
          
     // Weight Ticket Number
     int inWTNO
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0        
     }

     // Scale Ticket Number
     String inSTNO
     if (mi.in.get("STNO") != null) {
        inSTNO = mi.in.get("STNO") 
     } else {
        inSTNO = ""        
     }

     // Validate Delivery Detail record
     Optional<DBContainer> EXTDLD = findEXTDLD(inCONO, inDIVI, inDLNO)
     if(EXTDLD.isPresent()){
        mi.error("Delivery details already exist")   
        return             
     } else {
        // Write record 
        addEXTDLDRecord(inCONO, inDIVI, inDLNO, inCTNO, inWTNO, inSTNO)          
     }  
     
  }
  
  //******************************************************************** 
  // Get EXTDLD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLD(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLD").index("00").build()
     DBContainer EXTDLD = query.getContainer()
     EXTDLD.set("EXCONO", CONO)
     EXTDLD.set("EXDIVI", DIVI)
     EXTDLD.set("EXDLNO", DLNO)
     if(query.read(EXTDLD))  { 
       return Optional.of(EXTDLD)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Add EXTDLD record 
  //********************************************************************     
  void addEXTDLDRecord(int CONO, String DIVI, int DLNO, int CTNO, int WTNO, String STNO){  
       DBAction action = database.table("EXTDLD").index("00").build()
       DBContainer EXTDLD = action.createContainer()
       EXTDLD.set("EXCONO", CONO)
       EXTDLD.set("EXDIVI", DIVI)
       EXTDLD.set("EXDLNO", DLNO)
       EXTDLD.set("EXCTNO", CTNO)
       EXTDLD.set("EXWTNO", WTNO)
       EXTDLD.set("EXSTNO", STNO)   
       EXTDLD.set("EXCHID", program.getUser())
       EXTDLD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDLD.set("EXRGDT", regdate) 
       EXTDLD.set("EXLMDT", regdate) 
       EXTDLD.set("EXRGTM", regtime)
       action.insert(EXTDLD)         
  } 
     
} 

