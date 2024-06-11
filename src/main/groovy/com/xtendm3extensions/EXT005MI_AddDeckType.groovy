// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add deck type to EXTDPT
// Transaction AddDeckType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: TYPE - Deck Type
 * @param: NAME - Name
 * 
*/



public class AddDeckType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddDeckType(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Deck Type
     String inTYPE
     if (mi.in.get("TYPE") != null && mi.in.get("TYPE") != "") {
        inTYPE = mi.inData.get("TYPE").trim() 
     } else {
        inTYPE = ""         
     }
           
     // Name
     String inNAME 
     if (mi.in.get("NAME") != null && mi.in.get("NAME") != "") {
        inNAME = mi.inData.get("NAME").trim() 
     } else {
        inNAME= ""      
     }


     // Validate Deck Type record
     Optional<DBContainer> EXTDPT = findEXTDPT(inCONO, inDIVI, inTYPE)
     if(EXTDPT.isPresent()){
        mi.error("Deck Type already exists")   
        return             
     } else {
        // Write record 
        addEXTDPTRecord(inCONO, inDIVI, inTYPE, inNAME)          
     }  

  }
   

  //******************************************************************** 
  // Get EXTDPT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPT(int CONO, String DIVI, String TYPE){  
     DBAction query = database.table("EXTDPT").index("00").build()
     DBContainer EXTDPT = query.getContainer()
     EXTDPT.set("EXCONO", CONO)
     EXTDPT.set("EXDIVI", DIVI)
     EXTDPT.set("EXTYPE", TYPE)
     if(query.read(EXTDPT))  { 
       return Optional.of(EXTDPT)
     } 
  
     return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Add EXTDPT record 
  //********************************************************************     
  void addEXTDPTRecord(int CONO, String DIVI, String TYPE, String NAME){     
       DBAction action = database.table("EXTDPT").index("00").build()
       DBContainer EXTDPT = action.createContainer()
       EXTDPT.set("EXCONO", CONO)
       EXTDPT.set("EXDIVI", DIVI)
       EXTDPT.set("EXTYPE", TYPE)
       EXTDPT.set("EXNAME", NAME)   
       EXTDPT.set("EXCHID", program.getUser())
       EXTDPT.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDPT.set("EXRGDT", regdate) 
       EXTDPT.set("EXLMDT", regdate) 
       EXTDPT.set("EXRGTM", regtime)
       action.insert(EXTDPT)         
 } 

     
} 

