import subprocess
import flask
from flask import request, jsonify
app = flask.Flask(__name__)
app.config["DEBUG"] = True
@app.route('/',methods=['GET'])
def home():
	return "<h1>Api for MMAST</h1><p>Sample url: /api?eq=<eq>&op=<op>&arg1=<arg1>&arg2=<arg2>&</p>"

@app.route('/api',methods=['GET'])
def api_all():
	if 'eq' not in request.args or 'op' not in request.args:
	        return
	eq = request.args['eq']
	op = request.args['op']
	# cassie is the name of the compiled jar of the Clojuer CAS engine
	if('arg1' not in request.args):
		res = subprocess.run(['java','-jar','cassie.jar',eq,op],capture_output=True)
	elif ('arg2' not in request.args):
		res = subprocess.run(['java','-jar','cassie.jar',eq,op,request.args['arg1']],capture_output=True)
	else:
		res = subprocess.run(['java','-jar','cassie.jar',eq,op,request.args['arg1'],request.args['arg2']],capture_output=True)
	print(res.stdout)
	if res.stdout == None:
		return "None"
	else:
		return res.stdout.decode('utf-8')
if __name__ == '__main__':
	app.run()
