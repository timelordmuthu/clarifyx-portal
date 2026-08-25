import RequestCreationForm from "@/components/RequestCreationForm";

export default function Home() {
  return (
    <main className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-4xl mx-auto bg-white p-8 shadow-md rounded-lg">
        <h1 className="text-3xl font-bold text-center mb-8 text-blue-600">
          FAST/FACTS Sales Approval Portal
        </h1>
        {/* This line below brings your form onto the page! */}
        <RequestCreationForm />
      </div>
    </main>
  );
}