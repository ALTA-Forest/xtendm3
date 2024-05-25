// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add log header to EXTSLH
// Transaction AddLogHeader
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: STNO - Scale Ticket Number
 * @param: SEQN - Log Number
 * @param: TDCK - To Deck
 * @param: SPEC - Species
 * @param: ECOD - Exception Code
 * @param: TGNO - Tag Number
 * @param: LAMT - Amount
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: LGID - Log ID
 * 
**/


public class AddLogHeader extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inLGID
  int outLGID
  boolean numberFound
  Integer lastNumber
  String nextNumber

  // Constructor 
  public AddLogHeader(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Log Number
     int inSEQN  
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }
           
     // To Deck
     int inTDCK 
     if (mi.in.get("TDCK") != null) {
        inTDCK = mi.in.get("TDCK") 
     } else {
        inTDCK = 0        
     }
 
      // Species
     String inSPEC 
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = 0        
     }
     
     // Exception Code
     String inECOD  
     if (mi.in.get("ECOD") != null && mi.in.get("ECOD") != "") {
        inECOD = mi.inData.get("ECOD").trim() 
     } else {
        inECOD = ""        
     }

     // Tag Number
     String inTGNO
     if (mi.in.get("TGNO") != null && mi.in.get("TGNO") != "") {
        inTGNO = mi.inData.get("TGNO").trim() 
     } else {
        inTGNO = ""        
     }

     // Amount
     double inLAMT
     if (mi.in.get("LAMT") != null) {
        inLAMT = mi.in.get("LAMT") 
     } else {
        inLAMT = 0d       
     }

     // Validate Log Header record
     Optional<DBContainer> EXTSLH = findEXTSLH(inCONO, inDIVI, inSTID, inSEQN)
     if(EXTSLH.isPresent()){
        mi.error("Scale Ticket already exist")   
        return             
     } else {
        //Get next number for contract number
        getNextNumber("", "L7", "1") 
        outLGID = nextNumber as Integer
        inLGID = outLGID as Integer
     
        // Write record 
        addEXTSLHRecord(inCONO, inDIVI, inSTID, inSEQN, inTDCK, inSPEC, inECOD, inTGNO, inLGID, inLAMT)          
     }  
     
     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("LGID", String.valueOf(inLGID)) 
     mi.write()
  }


   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTSLH")
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTSLH")
     .index("10")
     .matching(expression)
     .selection("EXLGID")
     .reverse()
     .build()

     DBContainer line = actionline.getContainer() 
     
     line.set("EXCONO", inCONO)   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   } 

    
  //******************************************************************** 
  // List Last Number
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

      // Output
      if (!numberFound) {
        lastNumber = line.get("EXLGID") 
        numberFound = true
      }

  }
  
  
   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 
  
  //******************************************************************** 
  // Get EXTSLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSLH(int CONO, String DIVI, int STID, int SEQN){  
     DBAction query = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = query.getContainer()
     EXTSLH.set("EXCONO", CONO)
     EXTSLH.set("EXDIVI", DIVI)
     EXTSLH.set("EXSTID", STID)
     EXTSLH.set("EXSEQN", SEQN)
     if(query.read(EXTSLH))  { 
       return Optional.of(EXTSLH)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTSLH record 
  //********************************************************************     
  void addEXTSLHRecord(int CONO, String DIVI, int STID, int SEQN, int TDCK, String SPEC, String ECOD, String TGNO, int LGID, double LAMT){  
       DBAction action = database.table("EXTSLH").index("00").build()
       DBContainer EXTSLH = action.createContainer()
       EXTSLH.set("EXCONO", CONO)
       EXTSLH.set("EXDIVI", DIVI)
       EXTSLH.set("EXSTID", STID)
       EXTSLH.set("EXSEQN", SEQN)
       EXTSLH.set("EXTDCK", TDCK)
       EXTSLH.set("EXSPEC", SPEC)
       EXTSLH.set("EXECOD", ECOD)
       EXTSLH.set("EXTGNO", TGNO)
       EXTSLH.set("EXLGID", LGID)
       EXTSLH.set("EXLAMT", LAMT)       
       EXTSLH.set("EXCHID", program.getUser())
       EXTSLH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSLH.set("EXRGDT", regdate) 
       EXTSLH.set("EXLMDT", regdate) 
       EXTSLH.set("EXRGTM", regtime)
       action.insert(EXTSLH)         
 } 

     
} 

